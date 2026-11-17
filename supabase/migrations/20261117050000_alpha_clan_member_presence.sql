begin;

-- Store the last authenticated AlphaClient heartbeat for each current clan member.
create table if not exists public.clan_presence (
    profile_id uuid primary key references public.clan_members(profile_id) on delete cascade,
    online boolean not null default false,
    last_seen_at timestamptz not null default now()
);

create index if not exists clan_presence_last_seen_idx
    on public.clan_presence (last_seen_at desc);

alter table public.clan_presence enable row level security;
revoke all on public.clan_presence from anon, authenticated;
grant select, insert, update on public.clan_presence to authenticated;

drop policy if exists clan_presence_same_clan_read on public.clan_presence;
create policy clan_presence_same_clan_read
    on public.clan_presence for select to authenticated
    using (
        exists (
            select 1
            from public.clan_members viewer
            join public.clan_members target on target.clan_id = viewer.clan_id
            where viewer.profile_id = (select auth.uid())
              and target.profile_id = clan_presence.profile_id
        )
    );

drop policy if exists clan_presence_self_insert on public.clan_presence;
create policy clan_presence_self_insert
    on public.clan_presence for insert to authenticated
    with check (
        profile_id = (select auth.uid())
        and exists (
            select 1 from public.clan_members
            where profile_id = (select auth.uid())
        )
    );

drop policy if exists clan_presence_self_update on public.clan_presence;
create policy clan_presence_self_update
    on public.clan_presence for update to authenticated
    using (
        profile_id = (select auth.uid())
        and exists (
            select 1 from public.clan_members
            where profile_id = (select auth.uid())
        )
    )
    with check (
        profile_id = (select auth.uid())
        and exists (
            select 1 from public.clan_members
            where profile_id = (select auth.uid())
        )
    );

create or replace function public.clan_presence_heartbeat()
returns void
language plpgsql
security invoker
set search_path = ''
as $$
begin
    if auth.uid() is null then
        raise exception 'Authentication required.' using errcode = '42501';
    end if;

    if not exists (
        select 1 from public.clan_members
        where profile_id = auth.uid()
    ) then
        raise exception 'You must be in a clan to publish clan presence.' using errcode = '42501';
    end if;

    insert into public.clan_presence (profile_id, online, last_seen_at)
    values (auth.uid(), true, now())
    on conflict (profile_id) do update
        set online = true,
            last_seen_at = now();
end;
$$;

revoke all on function public.clan_presence_heartbeat() from public, anon;
grant execute on function public.clan_presence_heartbeat() to authenticated;

comment on table public.clan_presence is
    'AlphaClans member presence. Visible only to authenticated members of the same clan; stale heartbeats are displayed as offline by the client.';

commit;
