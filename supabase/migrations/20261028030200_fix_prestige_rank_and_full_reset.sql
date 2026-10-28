CREATE OR REPLACE FUNCTION public.pixel_garden_prestige_rank_multiplier(p_user uuid)
 RETURNS numeric
 LANGUAGE sql
 STABLE SECURITY DEFINER
 SET search_path TO 'public', 'auth'
AS $function$
  with assigned as (
    select lower(coalesce(p.rank_key,'')) as rank_key, coalesce(p.is_founder,false) as is_founder
      from public.profiles p where p.id=p_user
    union all
    select lower(coalesce(pr.rank_key,'')), false
      from public.profile_ranks pr where pr.profile_id=p_user
  ), ranked as (
    select case
      when is_founder or rank_key='founder' then 21
      when rank_key='owner' then 20
      when rank_key='developer' then 19
      when rank_key='manager' then 18
      when rank_key in ('platform_admin','platform admin','plat_admin') then 17
      when rank_key='sr_admin' or rank_key='senior_admin' then 16
      when rank_key='admin' then 15
      when rank_key in ('sr_moderator','senior_moderator') then 14
      when rank_key='moderator' then 13
      when rank_key in ('trial_moderator','t_mod','trial moderator') then 12
      when rank_key='partner' then 11
      when rank_key='famous' then 10
      when rank_key='media' then 9
      when rank_key in ('the_nibbler','nibbler') then 8
      when rank_key='beta' then 7
      when rank_key in ('alpha_plus','alpha+','alphaplus') then 6
      when rank_key='alpha' then 5
      when rank_key='elite' then 4
      when rank_key='rogue' then 3
      when rank_key='scout' then 2
      else 1 end as priority
    from assigned
  )
  select case coalesce(max(priority),1)
    when 2 then 1.1 when 3 then 1.2 when 4 then 1.3 when 5 then 1.4
    when 6 then 1.5 when 7 then 1.5 when 8 then 1.5 when 9 then 1.5
    when 10 then 1.5 when 11 then 1.5 when 12 then 1.5 when 13 then 1.6
    when 14 then 1.7 when 15 then 1.8 when 16 then 1.9
    when 17 then 2.0 when 18 then 2.0 when 19 then 2.0 when 20 then 2.0
    when 21 then 2.0 else 1.0 end from ranked;
$function$
;

CREATE OR REPLACE FUNCTION public.founder_pixel_garden_prestige_manage(p_action text, p_target_alpha_name text, p_amount numeric DEFAULT 0, p_multiplier numeric DEFAULT NULL::numeric)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public', 'auth'
AS $function$
declare actor uuid; target uuid; r public.pixel_garden_prestige%rowtype; next_coins numeric; next_save jsonb;
begin
  actor:=public.pixel_garden_require_founder();
  target:=public.pixel_garden_find_target(p_target_alpha_name);
  if p_action not in ('give_prestige_coins','take_prestige_coins','reset_prestige_coins','reset_prestige','full_reset','set_multiplier','reset_multiplier') then
    raise exception 'Unsupported Founder Prestige action.' using errcode='22023';
  end if;
  insert into public.pixel_garden_prestige(user_id) values(target) on conflict(user_id) do nothing;
  select * into r from public.pixel_garden_prestige where user_id=target for update;
  if p_action in ('give_prestige_coins','take_prestige_coins') and (p_amount<1 or p_amount>1000000 or p_amount<>trunc(p_amount)) then
    raise exception 'Amount must be a whole number between 1 and 1,000,000.' using errcode='22023'; end if;
  if p_action='give_prestige_coins' then
    update public.pixel_garden_prestige set prestige_coins=prestige_coins+p_amount,updated_at=now() where user_id=target;
  elsif p_action='take_prestige_coins' then
    update public.pixel_garden_prestige set prestige_coins=greatest(0,prestige_coins-p_amount),updated_at=now() where user_id=target;
  elsif p_action='reset_prestige_coins' then
    update public.pixel_garden_prestige set prestige_coins=0,updated_at=now() where user_id=target;
  elsif p_action='reset_multiplier' then
    update public.pixel_garden_prestige set multiplier_override=null,updated_at=now() where user_id=target;
  elsif p_action='set_multiplier' then
    if p_multiplier is null or p_multiplier<=0 or p_multiplier>5 then raise exception 'Multiplier must be greater than 0 and at most 5.0x.' using errcode='22023'; end if;
    update public.pixel_garden_prestige set multiplier_override=p_multiplier,updated_at=now() where user_id=target;
  elsif p_action='reset_prestige' then
    update public.pixel_garden_prestige set prestige_coins=0,active_seconds=0,last_heartbeat_at=null,prestige_count=0,upgrade_steps=0,multiplier_override=null,
      upgrades='{"prestige_yield":0,"prestige_bonus":0,"coin_cache":0}'::jsonb,updated_at=now() where user_id=target;
    select save_data into next_save from public.pixel_garden_saves where user_id=target for update;
    if next_save is null then next_save:=public.pixel_garden_default_save(); end if;
    next_save:=jsonb_set(next_save,'{purchased_upgrade_steps}',to_jsonb(0),true);
    insert into public.pixel_garden_saves(user_id,save_data,saved_at) values(target,next_save,now())
      on conflict(user_id) do update set save_data=excluded.save_data,saved_at=now();
  elsif p_action='full_reset' then
    update public.pixel_garden_prestige set prestige_coins=0,active_seconds=0,last_heartbeat_at=null,prestige_count=0,upgrade_steps=0,multiplier_override=null,
      upgrades='{"prestige_yield":0,"prestige_bonus":0,"coin_cache":0}'::jsonb,updated_at=now() where user_id=target;
    insert into public.pixel_garden_reset_tokens(user_id,token_count,updated_at) values(target,0,now())
      on conflict(user_id) do update set token_count=0,updated_at=now();
    insert into public.pixel_garden_saves(user_id,save_data,saved_at) values(target,public.pixel_garden_default_save() || jsonb_build_object('purchased_upgrade_steps',0),now())
      on conflict(user_id) do update set save_data=excluded.save_data,saved_at=now();
  end if;
  insert into public.audit_logs(actor_id,actor_label,product,category,action,target_user_id,target_label,outcome,details)
  values(actor,(select username from public.profiles where id=actor),'client','pixel_garden',p_action,target,
    (select username from public.profiles where id=target),'success',jsonb_build_object('amount',p_amount,'multiplier',p_multiplier,'alpha_name',p_target_alpha_name));
  select * into r from public.pixel_garden_prestige where user_id=target;
  return jsonb_build_object('success',true,'action',p_action,'target_alpha_name',(select username from public.profiles where id=target),
    'prestige_coins',r.prestige_coins,'prestige_count',r.prestige_count,'active_seconds',r.active_seconds,
    'multiplier_override',r.multiplier_override,'effective_multiplier',coalesce(r.multiplier_override,public.pixel_garden_prestige_rank_multiplier(target)));
end $function$
;
