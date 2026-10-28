-- Founder-only Pixel Garden coin grants by registered Alpha Account username.
-- Authorization is enforced by the database, not only by the client command.
create or replace function public.founder_grant_pixel_garden_coins(
  target_alpha_name text,
  coin_amount integer
)
returns jsonb
language plpgsql
security definer
set search_path = public, auth
as $$
declare
  actor_id uuid := auth.uid();
  target_id uuid;
  canonical_name text;
  current_save jsonb;
  current_coins integer;
  new_coins bigint;
  now_millis bigint := floor(extract(epoch from clock_timestamp()) * 1000);
begin
  if actor_id is null then
    raise exception 'Sign in to a Founder Alpha Account first.' using errcode = '42501';
  end if;
  if not exists (
    select 1 from public.profiles p
    where p.id = actor_id and coalesce(p.is_founder, false) = true
  ) then
    raise exception 'Only Founder can grant Pixel Garden coins.' using errcode = '42501';
  end if;
  if target_alpha_name is null or target_alpha_name !~ '^[A-Za-z0-9_]{1,16}$' then
    raise exception 'Enter a valid Alpha Account name (1-16 letters, numbers or underscores).';
  end if;
  if coin_amount is null or coin_amount < 1 or coin_amount > 1000000 then
    raise exception 'Amount must be between 1 and 1,000,000 coins.';
  end if;

  select p.id, p.username into target_id, canonical_name
  from public.profiles p
  where lower(p.username) = lower(target_alpha_name)
  order by p.id
  limit 1;
  if target_id is null then
    raise exception 'Alpha Account not found: %', target_alpha_name;
  end if;

  select s.save_data into current_save
  from public.pixel_garden_saves s where s.user_id = target_id for update;
  if current_save is null then
    current_save := jsonb_build_object(
      'version', 1,
      'pixelGardenUpgradeCoins', 100,
      'auto_harvest', false,
      'growth_levels', 0,
      'unlocked_plots', 1,
      'last_processed_at', now_millis,
      'seeds', jsonb_build_array(3, 0, 0, 0),
      'inventory', jsonb_build_array(0, 0, 0, 0)
    );
  end if;
  current_coins := greatest(0, coalesce((current_save ->> 'pixelGardenUpgradeCoins')::integer, 100));
  new_coins := current_coins::bigint + coin_amount::bigint;
  if new_coins > 2147483647 then
    raise exception 'Grant would exceed the maximum supported coin balance.';
  end if;
  current_save := jsonb_set(current_save, '{pixelGardenUpgradeCoins}', to_jsonb(new_coins::integer), true);
  current_save := jsonb_set(current_save, '{last_processed_at}', to_jsonb(now_millis), true);

  insert into public.pixel_garden_saves (user_id, save_data, saved_at)
  values (target_id, current_save, clock_timestamp())
  on conflict (user_id) do update
    set save_data = excluded.save_data, saved_at = excluded.saved_at;

  return jsonb_build_object(
    'alpha_name', canonical_name,
    'granted', coin_amount,
    'previous_coins', current_coins,
    'new_coins', new_coins
  );
end;
$$;

revoke all on function public.founder_grant_pixel_garden_coins(text, integer) from public, anon;
grant execute on function public.founder_grant_pixel_garden_coins(text, integer) to authenticated;
