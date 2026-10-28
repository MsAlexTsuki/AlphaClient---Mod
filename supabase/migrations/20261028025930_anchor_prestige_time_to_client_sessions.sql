-- Start a new active client session without crediting downtime.
create or replace function public.pixel_garden_prestige_begin_session()
returns jsonb language plpgsql security definer set search_path=public,auth as $$
declare u uuid:=auth.uid();
begin
  if u is null then raise exception 'Sign in to your Alpha Account first.' using errcode='42501'; end if;
  insert into public.pixel_garden_prestige(user_id) values(u) on conflict(user_id) do nothing;
  -- Reset the heartbeat anchor at process-session start; time between sessions is never credited.
  update public.pixel_garden_prestige set last_heartbeat_at=now(),updated_at=now() where user_id=u;
  return jsonb_build_object('success',true,'session_started_at',now());
end $$;
revoke all on function public.pixel_garden_prestige_begin_session() from public,anon;
grant execute on function public.pixel_garden_prestige_begin_session() to authenticated;
