
begin;
revoke all on function public.is_clan_member(uuid) from public, anon;
revoke all on function public.is_clan_manager(uuid) from public, anon;
grant execute on function public.is_clan_member(uuid) to authenticated;
grant execute on function public.is_clan_manager(uuid) to authenticated;
revoke all on function public.broadcast_clan_message() from public, anon, authenticated;
commit;
