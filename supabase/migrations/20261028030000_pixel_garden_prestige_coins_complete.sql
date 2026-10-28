-- AlphaClient Pixel Garden Prestige Coins.
-- Captures the database schema and server-side functions applied to Supabase project lescvpixttmmbnaluidd.
-- Depends on the existing Pixel Garden saves, profiles, profile_ranks and audit_logs schema.
create table if not exists public.pixel_garden_prestige (
  user_id uuid primary key references auth.users(id) on delete cascade,
  prestige_coins numeric(14,2) not null default 0 check (prestige_coins >= 0),
  active_seconds integer not null default 0 check (active_seconds >= 0 and active_seconds < 7200),
  last_heartbeat_at timestamptz,
  prestige_count integer not null default 0 check (prestige_count >= 0),
  upgrade_steps integer not null default 0 check (upgrade_steps >= 0),
  multiplier_override numeric(3,2) check (multiplier_override is null or (multiplier_override > 0 and multiplier_override <= 5.0)),
  upgrades jsonb not null default '{"prestige_yield":0,"prestige_bonus":0,"coin_cache":0}'::jsonb check (jsonb_typeof(upgrades) = 'object'),
  updated_at timestamptz not null default now()
);
alter table public.pixel_garden_prestige enable row level security;
drop policy if exists pixel_garden_prestige_read_own on public.pixel_garden_prestige;
create policy pixel_garden_prestige_read_own on public.pixel_garden_prestige for select to authenticated using (user_id = auth.uid());
revoke insert, update, delete on public.pixel_garden_prestige from anon, authenticated;
grant select on public.pixel_garden_prestige to authenticated;

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

CREATE OR REPLACE FUNCTION public.pixel_garden_buy_prestige_upgrade(p_upgrade text)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public', 'auth'
AS $function$
declare u uuid:=auth.uid(); r public.pixel_garden_prestige%rowtype; lvl integer; cost numeric; next_upgrades jsonb;
begin
  if u is null then raise exception 'Sign in first.' using errcode='42501'; end if;
  select * into r from public.pixel_garden_prestige where user_id=u for update;
  if not found then raise exception 'Prestige data is not initialized.' using errcode='P0001'; end if;
  if r.prestige_count<1 then raise exception 'You must Prestige at least once before buying Prestige upgrades.' using errcode='P0001'; end if;
  if r.upgrade_steps<18 then raise exception 'Buy 18 upgrade steps in Pixel Garden first.' using errcode='P0001'; end if;
  if p_upgrade not in ('prestige_yield','prestige_bonus','coin_cache') then raise exception 'Unknown Prestige upgrade.' using errcode='22023'; end if;
  lvl:=least(5,greatest(0,coalesce((r.upgrades->>p_upgrade)::integer,0)));
  if lvl>=5 then raise exception 'This Prestige upgrade is already at maximum level.' using errcode='P0001'; end if;
  cost:=case p_upgrade when 'prestige_yield' then (lvl+1)*25 when 'prestige_bonus' then (lvl+1)*40 else (lvl+1)*30 end;
  if r.prestige_coins<cost then raise exception 'Not enough Prestige Coins. Required: %.',cost using errcode='P0001'; end if;
  next_upgrades:=jsonb_set(r.upgrades,array[p_upgrade],to_jsonb(lvl+1),true);
  update public.pixel_garden_prestige set prestige_coins=prestige_coins-cost,upgrades=next_upgrades,updated_at=now() where user_id=u;
  return jsonb_build_object('success',true,'upgrade',p_upgrade,'level',lvl+1,'cost',cost,
    'prestige_coins',r.prestige_coins-cost,'upgrades',next_upgrades);
end $function$
;

CREATE OR REPLACE FUNCTION public.pixel_garden_prestige()
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public', 'auth'
AS $function$
declare u uuid:=auth.uid(); current_save jsonb; next_save jsonb; coins integer; r public.pixel_garden_prestige%rowtype; cache_level integer;
begin
  if u is null then raise exception 'Sign in to your Alpha Account first.' using errcode='42501'; end if;
  select save_data into current_save from public.pixel_garden_saves where user_id=u for update;
  if current_save is null then current_save:=public.pixel_garden_default_save(); end if;
  coins:=greatest(0,coalesce((current_save->>'pixelGardenUpgradeCoins')::integer,100));
  if coins<20000 then raise exception 'Prestige costs 20,000 coins.' using errcode='P0001'; end if;
  select * into r from public.pixel_garden_prestige where user_id=u for update;
  if not found or r.upgrade_steps<18 then raise exception 'Buy 18 upgrade steps in the Upgrade tab before using Prestige.' using errcode='P0001'; end if;
  cache_level:=least(5,greatest(0,coalesce((r.upgrades->>'coin_cache')::integer,0)));
  next_save:=public.pixel_garden_default_save() || jsonb_build_object('more_coins_level',0,'more_crops_level',0,
    'pixelGardenUpgradeCoins',100+cache_level*250,'purchased_upgrade_steps',r.upgrade_steps);
  update public.pixel_garden_prestige set prestige_count=prestige_count+1,updated_at=now() where user_id=u;
  insert into public.pixel_garden_saves(user_id,save_data,saved_at) values(u,next_save,now())
    on conflict(user_id) do update set save_data=excluded.save_data,saved_at=now();
  return jsonb_build_object('success',true,'reset_type','prestige','coins_spent',20000,'prestige_count',r.prestige_count+1);
end $function$
;

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
    -- Count only regular client heartbeats. A delayed call cannot credit offline time.
    if elapsed < 1 or elapsed > 35 then elapsed := 0; end if;
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
      when rank_key in ('platform_admin','platform admin') then 17
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

CREATE OR REPLACE FUNCTION public.pixel_garden_prestige_state()
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public', 'auth'
AS $function$
declare u uuid := auth.uid(); r public.pixel_garden_prestige%rowtype; rank_mult numeric; effective_mult numeric;
begin
  if u is null then raise exception 'Sign in to your Alpha Account first.' using errcode='42501'; end if;
  insert into public.pixel_garden_prestige(user_id) values(u) on conflict(user_id) do nothing;
  select * into r from public.pixel_garden_prestige where user_id=u;
  rank_mult := public.pixel_garden_prestige_rank_multiplier(u);
  effective_mult := coalesce(r.multiplier_override,rank_mult);
  return jsonb_build_object('prestige_coins',r.prestige_coins,'active_seconds',r.active_seconds,
    'seconds_to_reward',7200-r.active_seconds,'prestige_count',r.prestige_count,
    'upgrade_steps',r.upgrade_steps,'unlocked',r.upgrade_steps>=18,
    'rank_multiplier',rank_mult,'effective_multiplier',effective_mult,
    'multiplier_source',case when r.multiplier_override is null then 'rank' else 'founder_override' end,
    'multiplier_override',r.multiplier_override,'upgrades',r.upgrades);
end $function$
;

CREATE OR REPLACE FUNCTION public.pixel_garden_purchase_upgrade(p_upgrade text)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public', 'auth'
AS $function$
declare u uuid:=auth.uid(); s jsonb; coins integer; steps integer; cost integer; level integer; key text;
begin
 if u is null then raise exception 'Sign in first.' using errcode='42501'; end if;
 if p_upgrade not in ('auto_harvest','growth_boost','more_coins','more_crops','expansion') then raise exception 'Unknown garden upgrade.' using errcode='22023'; end if;
 select save_data into s from public.pixel_garden_saves where user_id=u for update;
 if s is null then s:=public.pixel_garden_default_save(); end if;
 coins:=greatest(0,coalesce((s->>'pixelGardenUpgradeCoins')::integer,100));
 steps:=greatest(0,coalesce((s->>'purchased_upgrade_steps')::integer,0));
 if p_upgrade='auto_harvest' then
   if coalesce((s->>'auto_harvest')::boolean,false) then raise exception 'Auto Harvest is already owned.' using errcode='P0001'; end if;
   cost:=2000; if coins<cost then raise exception 'Not enough Pixel Garden Coins.' using errcode='P0001'; end if;
   s:=jsonb_set(s,'{auto_harvest}','true'::jsonb,true);
 elsif p_upgrade='growth_boost' then
   level:=greatest(0,coalesce((s->>'growth_levels')::integer,0));
   if level>=3 then raise exception 'Growth Boost is at maximum level.' using errcode='P0001'; end if;
   cost:=1250*(level+1); if coins<cost then raise exception 'Not enough Pixel Garden Coins.' using errcode='P0001'; end if;
   s:=jsonb_set(s,'{growth_levels}',to_jsonb(level+1),true);
 elsif p_upgrade='more_coins' then
   level:=greatest(0,coalesce((s->>'more_coins_level')::integer,0));
   if level>=5 then raise exception 'More Coins is at maximum level.' using errcode='P0001'; end if;
   cost:=(array[500,700,1000,1500,2000])[level+1]; if coins<cost then raise exception 'Not enough Pixel Garden Coins.' using errcode='P0001'; end if;
   s:=jsonb_set(s,'{more_coins_level}',to_jsonb(level+1),true);
 elsif p_upgrade='more_crops' then
   level:=greatest(0,coalesce((s->>'more_crops_level')::integer,0));
   if level>=6 then raise exception 'More Crops is at maximum level.' using errcode='P0001'; end if;
   cost:=(array[750,1000,1250,1500,1750,2000])[level+1]; if coins<cost then raise exception 'Not enough Pixel Garden Coins.' using errcode='P0001'; end if;
   s:=jsonb_set(s,'{more_crops_level}',to_jsonb(level+1),true);
 else
   level:=greatest(0,coalesce((s->>'unlocked_plots')::integer,1))-1;
   if level>=27 then raise exception 'Garden expansion is at maximum size.' using errcode='P0001'; end if;
   cost:=(array[50,100,150,250,300,400,450,500,600,700,800,900,1000,1150,1300,1500,2000,2500,3000,4000,5000,5500,5600,5700,5800,5900,6000])[level+1];
   if cost is null then raise exception 'Garden expansion is unavailable.' using errcode='P0001'; end if;
   if coins<cost then raise exception 'Not enough Pixel Garden Coins.' using errcode='P0001'; end if;
   s:=jsonb_set(s,'{unlocked_plots}',to_jsonb(least(28,level+2)),true);
 end if;
 s:=jsonb_set(s,'{pixelGardenUpgradeCoins}',to_jsonb(coins-cost),true);
 steps:=steps+1;
 s:=jsonb_set(s,'{purchased_upgrade_steps}',to_jsonb(steps),true);
 insert into public.pixel_garden_saves(user_id,save_data,saved_at) values(u,s,now())
 on conflict(user_id) do update set save_data=excluded.save_data,saved_at=now();
 insert into public.pixel_garden_prestige(user_id,upgrade_steps) values(u,steps)
 on conflict(user_id) do update set upgrade_steps=greatest(public.pixel_garden_prestige.upgrade_steps,excluded.upgrade_steps),updated_at=now();
 return jsonb_build_object('success',true,'upgrade',p_upgrade,'cost',cost,'coins',coins-cost,'upgrade_steps',steps,'unlocked',steps>=18,'save_data',s);
end $function$
;

CREATE OR REPLACE FUNCTION public.pixel_garden_record_upgrade_step(p_steps integer DEFAULT 1)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public', 'auth'
AS $function$
declare u uuid:=auth.uid(); current_save jsonb; steps integer;
begin
  if u is null then raise exception 'Sign in first.' using errcode='42501'; end if;
  if p_steps<1 or p_steps>28 then raise exception 'Invalid upgrade-step count.' using errcode='22023'; end if;
  select save_data into current_save from public.pixel_garden_saves where user_id=u for update;
  if current_save is null then current_save:=public.pixel_garden_default_save(); end if;
  steps:=greatest(0,coalesce((current_save->>'purchased_upgrade_steps')::integer,0))+p_steps;
  current_save:=jsonb_set(current_save,'{purchased_upgrade_steps}',to_jsonb(steps),true);
  insert into public.pixel_garden_saves(user_id,save_data,saved_at) values(u,current_save,now())
    on conflict(user_id) do update set save_data=excluded.save_data,saved_at=now();
  insert into public.pixel_garden_prestige(user_id,upgrade_steps) values(u,steps)
    on conflict(user_id) do update set upgrade_steps=greatest(public.pixel_garden_prestige.upgrade_steps,excluded.upgrade_steps),updated_at=now();
  return jsonb_build_object('upgrade_steps',steps,'unlocked',steps>=18);
end $function$
;

revoke all on function public.pixel_garden_prestige_rank_multiplier(uuid) from public,anon;
revoke all on function public.pixel_garden_prestige_state() from public,anon;
revoke all on function public.pixel_garden_prestige_heartbeat() from public,anon;
revoke all on function public.pixel_garden_record_upgrade_step(integer) from public,anon,authenticated;
revoke all on function public.pixel_garden_purchase_upgrade(text) from public,anon;
revoke all on function public.pixel_garden_buy_prestige_upgrade(text) from public,anon;
revoke all on function public.pixel_garden_prestige() from public,anon;
revoke all on function public.founder_pixel_garden_prestige_manage(text,text,numeric,numeric) from public,anon;
grant execute on function public.pixel_garden_prestige_state() to authenticated;
grant execute on function public.pixel_garden_prestige_heartbeat() to authenticated;
grant execute on function public.pixel_garden_purchase_upgrade(text) to authenticated;
grant execute on function public.pixel_garden_buy_prestige_upgrade(text) to authenticated;
grant execute on function public.pixel_garden_prestige() to authenticated;
grant execute on function public.founder_pixel_garden_prestige_manage(text,text,numeric,numeric) to authenticated;
