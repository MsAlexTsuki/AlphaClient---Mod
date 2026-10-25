-- Staff Operations: live presence and private manager notes.
-- Presence is shared with authorized staff. Notes are private to their author,
-- except Owner/Founder who may inspect all manager notes.
create or replace function public.staff_operations_is_manager_or_higher()
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select exists (
    select 1 from public.profiles p
    where p.id = auth.uid()
      and (p.is_founder or p.rank_key in ('manager','owner','founder'))
  );
$$;
revoke all on function public.staff_operations_is_manager_or_higher() from public, anon;
grant execute on function public.staff_operations_is_manager_or_higher() to authenticated;

create or replace function public.staff_operations_is_owner_or_founder()
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select exists (
    select 1 from public.profiles p
    where p.id = auth.uid()
      and (p.is_founder or p.rank_key in ('owner','founder'))
  );
$$;
revoke all on function public.staff_operations_is_owner_or_founder() from public, anon;
grant execute on function public.staff_operations_is_owner_or_founder() to authenticated;

create table if not exists public.staff_presence (
  user_id uuid primary key references auth.users(id) on delete cascade,
  alpha_name text not null default '',
  online boolean not null default false,
  afk boolean not null default false,
  last_seen_at timestamptz not null default now(),
  last_heartbeat_at timestamptz not null default now(),
  session_started_at timestamptz,
  updated_at timestamptz not null default now()
);
create index if not exists staff_presence_online_heartbeat_idx
  on public.staff_presence (online, last_heartbeat_at desc);
alter table public.staff_presence enable row level security;
revoke all on public.staff_presence from anon, authenticated;
grant select, insert, update on public.staff_presence to authenticated;

drop policy if exists staff_presence_staff_read on public.staff_presence;
create policy staff_presence_staff_read on public.staff_presence
  for select to authenticated using (public.staff_operations_is_staff());
drop policy if exists staff_presence_self_insert on public.staff_presence;
create policy staff_presence_self_insert on public.staff_presence
  for insert to authenticated with check (user_id = auth.uid() and public.staff_operations_is_staff());
drop policy if exists staff_presence_self_update on public.staff_presence;
create policy staff_presence_self_update on public.staff_presence
  for update to authenticated using (user_id = auth.uid() and public.staff_operations_is_staff())
  with check (user_id = auth.uid() and public.staff_operations_is_staff());

create or replace function public.staff_presence_heartbeat(p_alpha_name text, p_afk boolean)
returns public.staff_presence
language plpgsql security invoker
set search_path = public
as $$
declare result public.staff_presence;
begin
  if auth.uid() is null or not public.staff_operations_is_staff() then
    raise exception 'Staff access required';
  end if;
  insert into public.staff_presence(user_id, alpha_name, online, afk, last_seen_at, last_heartbeat_at, session_started_at, updated_at)
  values (auth.uid(), left(coalesce(p_alpha_name, ''), 64), true, coalesce(p_afk, false), now(), now(), now(), now())
  on conflict (user_id) do update set
    alpha_name = excluded.alpha_name,
    online = true,
    afk = excluded.afk,
    last_seen_at = now(),
    last_heartbeat_at = now(),
    session_started_at = case when public.staff_presence.online then public.staff_presence.session_started_at else now() end,
    updated_at = now()
  returning * into result;
  return result;
end;
$$;
revoke all on function public.staff_presence_heartbeat(text, boolean) from public, anon;
grant execute on function public.staff_presence_heartbeat(text, boolean) to authenticated;

create or replace function public.staff_presence_mark_offline()
returns void language plpgsql security invoker set search_path = public as $$
begin
  update public.staff_presence set online = false, afk = false, last_seen_at = now(), updated_at = now()
  where user_id = auth.uid();
end;
$$;
revoke all on function public.staff_presence_mark_offline() from public, anon;
grant execute on function public.staff_presence_mark_offline() to authenticated;

-- Heartbeat expiry ensures crashes and forced disconnects eventually appear offline.
create or replace function public.staff_presence_expire_stale(p_timeout_seconds integer default 90)
returns integer language plpgsql security definer set search_path = '' as $$
declare changed integer;
begin
  update public.staff_presence set online = false, afk = false, updated_at = now()
  where online = true and last_heartbeat_at < now() - make_interval(secs => greatest(30, least(p_timeout_seconds, 3600)));
  get diagnostics changed = row_count;
  return changed;
end;
$$;
revoke all on function public.staff_presence_expire_stale(integer) from public, anon, authenticated;

create table if not exists public.staff_private_notes (
  id uuid primary key default gen_random_uuid(),
  subject_name text not null check (subject_name ~ '^[A-Za-z0-9_]{1,16}$'),
  note text not null check (length(trim(note)) between 1 and 2000),
  created_by uuid not null references auth.users(id) on delete cascade,
  created_by_label text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index if not exists staff_private_notes_author_created_idx
  on public.staff_private_notes (created_by, created_at desc);
create index if not exists staff_private_notes_subject_idx
  on public.staff_private_notes (lower(subject_name), created_at desc);
alter table public.staff_private_notes enable row level security;
revoke all on public.staff_private_notes from anon, authenticated;
grant select, insert, update, delete on public.staff_private_notes to authenticated;

drop policy if exists staff_private_notes_read on public.staff_private_notes;
create policy staff_private_notes_read on public.staff_private_notes
  for select to authenticated using (
    public.staff_operations_is_manager_or_higher()
    and (created_by = auth.uid() or public.staff_operations_is_owner_or_founder())
  );
drop policy if exists staff_private_notes_insert on public.staff_private_notes;
create policy staff_private_notes_insert on public.staff_private_notes
  for insert to authenticated with check (
    created_by = auth.uid()
    and public.staff_operations_is_manager_or_higher()
  );
drop policy if exists staff_private_notes_update on public.staff_private_notes;
create policy staff_private_notes_update on public.staff_private_notes
  for update to authenticated using (
    public.staff_operations_is_manager_or_higher()
    and (created_by = auth.uid() or public.staff_operations_is_owner_or_founder())
  ) with check (
    public.staff_operations_is_manager_or_higher()
    and (created_by = auth.uid() or public.staff_operations_is_owner_or_founder())
  );
drop policy if exists staff_private_notes_delete on public.staff_private_notes;
create policy staff_private_notes_delete on public.staff_private_notes
  for delete to authenticated using (
    public.staff_operations_is_manager_or_higher()
    and (created_by = auth.uid() or public.staff_operations_is_owner_or_founder())
  );

comment on table public.staff_presence is 'Live staff online/AFK status, updated by authenticated client heartbeats.';
comment on table public.staff_private_notes is 'Private staff moderation notes: author-only, except Owner/Founder oversight.';


-- Run stale-session cleanup every minute. pg_cron is available on Supabase,
-- but is not enabled in every project by default.
create extension if not exists pg_cron with schema pg_catalog;
do $$
begin
  if not exists (select 1 from cron.job where jobname = 'staff-presence-expire-stale') then
    perform cron.schedule(
      'staff-presence-expire-stale',
      '* * * * *',
      $job$select public.staff_presence_expire_stale(90);$job$
    );
  end if;
end;
$$;
