-- Allow invite recipients and join-request owners to read the related clan row.
-- Clan browser visibility is handled separately by get_clan_browser().
drop policy if exists clans_invite_recipient_read on public.clans;
create policy clans_invite_recipient_read on public.clans
for select to authenticated
using (exists (
  select 1 from public.clan_invites i
  where i.clan_id = clans.id and i.target_profile_id = (select auth.uid())
));
drop policy if exists clans_join_requester_read on public.clans;
create policy clans_join_requester_read on public.clans
for select to authenticated
using (exists (
  select 1 from public.clan_join_requests r
  where r.clan_id = clans.id and r.profile_id = (select auth.uid())
));
