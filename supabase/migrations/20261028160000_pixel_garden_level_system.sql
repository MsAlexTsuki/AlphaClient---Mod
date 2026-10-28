-- Pixel Garden level management. Server-side account checks are mandatory.
create or replace function public.pixel_garden_level_command(
  p_action text,
  p_target_alpha_name text default null,
  p_level_value integer default 0,
  p_xp_value bigint default 0,
  p_text_value text default null
)
returns jsonb
language plpgsql
security definer
set search_path = public, auth
as $$
declare
  actor_id uuid := auth.uid();
  target_id uuid;
  actor_founder boolean := false;
  target_name text;
  save_data jsonb;
  progression jsonb;
  current_level integer;
  current_xp bigint;
  required_xp bigint;
  tokens integer;
  normalized_action text := lower(coalesce(p_action, ''));
  level_key text;
begin
  if actor_id is null then
    raise exception 'Sign in to an Alpha Account first.' using errcode = '42501';
  end if;
  select coalesce(p.is_founder, false) into actor_founder from public.profiles p where p.id = actor_id;
  if p_target_alpha_name is null or btrim(p_target_alpha_name) = '' then
    target_id := actor_id;
    select p.username into target_name from public.profiles p where p.id = target_id;
  else
    if not coalesce(actor_founder, false) and normalized_action not in ('info', 'check_level', 'check_xp') then
      raise exception 'Only Founder can access another account''s level data for management.' using errcode = '42501';
    end if;
    if p_target_alpha_name !~ '^[A-Za-z0-9_]{1,16}$' then
      raise exception 'Enter a valid Alpha Account name.';
    end if;
    select p.id, p.username into target_id, target_name
      from public.profiles p where lower(p.username) = lower(p_target_alpha_name) limit 1;
    if target_id is null then raise exception 'Alpha Account not found: %', p_target_alpha_name; end if;
  end if;

  select s.save_data into save_data from public.pixel_garden_saves s where s.user_id = target_id for update;
  if save_data is null then save_data := jsonb_build_object('version', 2, 'progression', jsonb_build_object('level', 1, 'experience', 0, 'reset_level_tokens', 0)); end if;
  progression := coalesce(save_data -> 'progression', '{}'::jsonb);
  current_level := greatest(1, coalesce((progression ->> 'level')::integer, 1));
  current_xp := greatest(0, coalesce((progression ->> 'experience')::bigint, 0));
  tokens := greatest(0, coalesce((progression ->> 'reset_level_tokens')::integer, 0));
  level_key := current_level::text;
  required_xp := greatest(1, coalesce((progression -> 'level_xp_requirements' ->> level_key)::bigint,
    case
      when current_level <= 10 then current_level * 5
      when current_level <= 15 then 50 + (current_level - 10) * 10
      when current_level <= 20 then 100 + (current_level - 15) * 20
      when current_level = 21 then 230 when current_level = 22 then 260 when current_level = 23 then 280
      when current_level = 24 then 290 when current_level = 25 then 300 when current_level = 26 then 320
      when current_level = 27 then 340 when current_level = 28 then 360 when current_level = 29 then 380
      when current_level = 30 then 400
      when current_level <= 39 then 500 + (current_level - 31) * 50
      when current_level = 40 then 1000 when current_level = 41 then 1010
      when current_level = 42 then 1015 when current_level = 43 then 1020
      when current_level = 44 then 1030 when current_level = 100 then 1600
      when current_level >= 101 then 1600 + (current_level - 100) * 10
      when current_level >= 45 then 1040 + (current_level - 45) * 10
      else 5 end));

  if normalized_action in ('info', 'check_level', 'check_xp') then
    return jsonb_build_object('alpha_name', target_name, 'level', current_level, 'experience', current_xp,
      'required_xp', required_xp, 'xp_remaining', greatest(0, required_xp - current_xp), 'reset_level_tokens', tokens);
  end if;

  if normalized_action in ('setlevel', 'resetlevel', 'give_token', 'take_token', 'edit_xp', 'edit_rewards', 'edit_desc', 'create_level', 'delete_level', 'edit_message') and not coalesce(actor_founder, false) then
    raise exception 'Only Founder can perform this level-management action.' using errcode = '42501';
  end if;

  if normalized_action = 'setlevel' then
    if p_level_value < 1 or p_level_value > 100000 then raise exception 'Level must be between 1 and 100000.'; end if;
    progression := jsonb_set(jsonb_set(progression, '{level}', to_jsonb(p_level_value), true), '{experience}', '0'::jsonb, true);
  elsif normalized_action = 'resetlevel' then
    progression := jsonb_set(jsonb_set(progression, '{level}', '1'::jsonb, true), '{experience}', '0'::jsonb, true);
  elsif normalized_action = 'give_token' then
    progression := jsonb_set(progression, '{reset_level_tokens}', to_jsonb(tokens + greatest(1, p_level_value)), true);
  elsif normalized_action = 'take_token' then
    progression := jsonb_set(progression, '{reset_level_tokens}', to_jsonb(greatest(0, tokens - greatest(1, p_level_value))), true);
  elsif normalized_action = 'use_token' then
    if tokens < 1 then raise exception 'You do not have a level reset token.'; end if;
    progression := jsonb_set(jsonb_set(progression, '{reset_level_tokens}', to_jsonb(tokens - 1), true), '{level}', '1'::jsonb, true);
    progression := jsonb_set(progression, '{experience}', '0'::jsonb, true);
  elsif normalized_action = 'edit_xp' then
    if p_level_value < 1 or p_xp_value < 1 then raise exception 'Level and XP must be positive.'; end if;
    progression := jsonb_set(progression, array['level_xp_requirements', p_level_value::text], to_jsonb(p_xp_value), true);
    update public.pixel_garden_saves s
      set save_data = jsonb_set(s.save_data, '{progression,level_xp_requirements}', coalesce(s.save_data #> '{progression,level_xp_requirements}', '{}'::jsonb) || jsonb_build_object(p_level_value::text, p_xp_value), true),
          saved_at = clock_timestamp()
      where s.user_id <> target_id;
  elsif normalized_action = 'edit_rewards' then
    if p_level_value < 1 then raise exception 'Level must be positive.'; end if;
    progression := jsonb_set(progression, array['level_rewards', p_level_value::text], to_jsonb(coalesce(p_text_value, '')), true);
    update public.pixel_garden_saves s set save_data = jsonb_set(s.save_data, '{progression,level_rewards}', coalesce(s.save_data #> '{progression,level_rewards}', '{}'::jsonb) || jsonb_build_object(p_level_value::text, coalesce(p_text_value, '')), true), saved_at = clock_timestamp() where s.user_id <> target_id;
  elsif normalized_action = 'edit_desc' then
    if p_level_value < 1 then raise exception 'Level must be positive.'; end if;
    progression := jsonb_set(progression, array['level_descriptions', p_level_value::text], to_jsonb(coalesce(p_text_value, '')), true);
    update public.pixel_garden_saves s set save_data = jsonb_set(s.save_data, '{progression,level_descriptions}', coalesce(s.save_data #> '{progression,level_descriptions}', '{}'::jsonb) || jsonb_build_object(p_level_value::text, coalesce(p_text_value, '')), true), saved_at = clock_timestamp() where s.user_id <> target_id;
  elsif normalized_action = 'create_level' then
    if p_level_value < 1 or p_xp_value < 1 then raise exception 'Level and XP must be positive.'; end if;
    progression := jsonb_set(progression, array['level_xp_requirements', p_level_value::text], to_jsonb(p_xp_value), true);
    progression := jsonb_set(progression, array['level_rewards', p_level_value::text], to_jsonb(coalesce(p_text_value, '')), true);
    update public.pixel_garden_saves s set save_data = jsonb_set(jsonb_set(s.save_data, '{progression,level_xp_requirements}', coalesce(s.save_data #> '{progression,level_xp_requirements}', '{}'::jsonb) || jsonb_build_object(p_level_value::text, p_xp_value), true), '{progression,level_rewards}', coalesce(s.save_data #> '{progression,level_rewards}', '{}'::jsonb) || jsonb_build_object(p_level_value::text, coalesce(p_text_value, '')), true), saved_at = clock_timestamp() where s.user_id <> target_id;
  elsif normalized_action = 'delete_level' then
    if p_level_value < 1 or p_level_value = current_level then raise exception 'Cannot delete the current level.'; end if;
    progression := jsonb_set(progression, '{level_xp_requirements}', coalesce(progression -> 'level_xp_requirements', '{}'::jsonb) - p_level_value::text, true);
    progression := jsonb_set(progression, '{level_rewards}', coalesce(progression -> 'level_rewards', '{}'::jsonb) - p_level_value::text, true);
    progression := jsonb_set(progression, '{level_descriptions}', coalesce(progression -> 'level_descriptions', '{}'::jsonb) - p_level_value::text, true);
    update public.pixel_garden_saves s set save_data = jsonb_set(jsonb_set(jsonb_set(s.save_data, '{progression,level_xp_requirements}', coalesce(s.save_data #> '{progression,level_xp_requirements}', '{}'::jsonb) - p_level_value::text, true), '{progression,level_rewards}', coalesce(s.save_data #> '{progression,level_rewards}', '{}'::jsonb) - p_level_value::text, true), '{progression,level_descriptions}', coalesce(s.save_data #> '{progression,level_descriptions}', '{}'::jsonb) - p_level_value::text, true), saved_at = clock_timestamp() where s.user_id <> target_id;
  elsif normalized_action = 'edit_message' then
    progression := jsonb_set(progression, '{custom_level_message}', to_jsonb(left(coalesce(p_text_value, ''), 240)), true);
    update public.pixel_garden_saves s set save_data = jsonb_set(s.save_data, '{progression,custom_level_message}', to_jsonb(left(coalesce(p_text_value, ''), 240)), true), saved_at = clock_timestamp() where s.user_id <> target_id;
  else
    raise exception 'Unknown Pixel Garden level action: %', normalized_action;
  end if;

  save_data := jsonb_set(save_data, '{progression}', progression, true);
  insert into public.pixel_garden_saves(user_id, save_data, saved_at) values(target_id, save_data, clock_timestamp())
    on conflict(user_id) do update set save_data = excluded.save_data, saved_at = excluded.saved_at;
  return jsonb_build_object('success', true, 'alpha_name', target_name, 'action', normalized_action,
    'level', greatest(1, coalesce((progression ->> 'level')::integer, 1)),
    'experience', greatest(0, coalesce((progression ->> 'experience')::bigint, 0)),
    'reset_level_tokens', greatest(0, coalesce((progression ->> 'reset_level_tokens')::integer, 0)));
end;
$$;
revoke all on function public.pixel_garden_level_command(text, text, integer, bigint, text) from public, anon;
grant execute on function public.pixel_garden_level_command(text, text, integer, bigint, text) to authenticated;
