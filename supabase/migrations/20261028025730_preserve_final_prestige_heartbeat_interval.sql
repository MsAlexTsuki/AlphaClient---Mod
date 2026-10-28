CREATE OR REPLACE FUNCTION public.pixel_garden_prestige_heartbeat()
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public', 'auth'
AS $function$
declare u uuid:=auth.uid(); r public.pixel_garden_prestige%rowtype; elapsed integer:=0;
  rank_mult numeric; effective_mult numeric; yield_level integer; bonus_level integer; award numeric:=0;
begin
  if u is null then raise exception 'Sign in to your Alpha Account first.' using errcode='42501'; end if;
  insert into public.pixel_garden_prestige(user_id) values(u) on conflict(user_id) do nothing;
  select * into r from public.pixel_garden_prestige where user_id=u for update;
  if r.last_heartbeat_at is not null then
    elapsed := floor(extract(epoch from (now()-r.last_heartbeat_at)))::integer;
    -- Count regular heartbeat gaps, and at most the last 15-second interval after a restart.
    -- Long gaps never credit offline time; the small final interval is not lost on abrupt shutdown.
    if elapsed < 1 then elapsed := 0;
    elsif elapsed > 35 then elapsed := 15;
    end if;
  end if;
  r.active_seconds := r.active_seconds + elapsed;
  rank_mult := public.pixel_garden_prestige_rank_multiplier(u);
  effective_mult := coalesce(r.multiplier_override,rank_mult);
  yield_level := least(5,greatest(0,coalesce((r.upgrades->>'prestige_yield')::integer,0)));
  bonus_level := least(5,greatest(0,coalesce((r.upgrades->>'prestige_bonus')::integer,0)));
  if r.active_seconds >= 7200 then
    award := floor(r.active_seconds/7200.0) * (2 + yield_level) * effective_mult * (1 + bonus_level*0.10);
    r.active_seconds := r.active_seconds % 7200;
    r.prestige_coins := r.prestige_coins + award;
  end if;
  update public.pixel_garden_prestige set prestige_coins=r.prestige_coins, active_seconds=r.active_seconds,
    last_heartbeat_at=now(), updated_at=now() where user_id=u;
  return jsonb_build_object('prestige_coins',r.prestige_coins,'active_seconds',r.active_seconds,
    'award',award,'seconds_to_reward',7200-r.active_seconds,
    'effective_multiplier',effective_mult,'rank_multiplier',rank_mult,
    'multiplier_source',case when r.multiplier_override is null then 'rank' else 'founder_override' end,
    'prestige_count',r.prestige_count,'upgrade_steps',r.upgrade_steps,'unlocked',r.upgrade_steps>=18,'upgrades',r.upgrades);
end $function$
;
