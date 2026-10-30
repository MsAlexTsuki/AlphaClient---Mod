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
revoke all on function public.pixel_garden_purchase_upgrade(text) from public,anon;
grant execute on function public.pixel_garden_purchase_upgrade(text) to authenticated;
