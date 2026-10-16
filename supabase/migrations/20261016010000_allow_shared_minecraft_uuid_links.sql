-- Allow a Minecraft UUID to be associated with multiple Alpha profiles.
-- This is a display association only; it is not proof of ownership or authorization.
-- Apply after the existing community rank catalog and alpha_minecraft_accounts migrations.

begin;

do $$
declare
  primary_key_name text;
begin
  select c.conname into primary_key_name
  from pg_constraint c
  where c.conrelid = 'public.alpha_minecraft_accounts'::regclass
    and c.contype = 'p';

  if primary_key_name is not null then
    execute format('alter table public.alpha_minecraft_accounts drop constraint %I', primary_key_name);
  end if;
end;
$$;

alter table public.alpha_minecraft_accounts
  add constraint alpha_minecraft_accounts_pkey primary key (mc_uuid, profile_id);

create index if not exists alpha_minecraft_accounts_profile_id_idx
  on public.alpha_minecraft_accounts(profile_id);

create unique index if not exists alpha_minecraft_accounts_profile_uuid_idx
  on public.alpha_minecraft_accounts(profile_id, mc_uuid);

create or replace function public.link_minecraft_uuid(target_uuid uuid)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
  current_profile_id uuid;
begin
  if auth.uid() is null then
    raise exception 'Authentication required.' using errcode = '42501';
  end if;
  if target_uuid is null then
    raise exception 'Minecraft UUID is required.' using errcode = '22023';
  end if;

  select p.id into current_profile_id
  from public.profiles p
  where p.id = auth.uid();

  if current_profile_id is null then
    raise exception 'Alpha community profile not found.' using errcode = 'P0002';
  end if;

  insert into public.alpha_minecraft_accounts (mc_uuid, profile_id)
  values (target_uuid, current_profile_id)
  on conflict (mc_uuid, profile_id) do nothing;

  return jsonb_build_object('minecraft_uuid', target_uuid);
end;
$$;

revoke all on function public.link_minecraft_uuid(uuid) from public, anon;
grant execute on function public.link_minecraft_uuid(uuid) to authenticated;

-- Public lookup retains the existing (mc_uuid, rank) response shape.
-- When a UUID belongs to multiple profiles, use the most recently linked profile
-- for public/other-player display. The local player's badge is rendered from the
-- currently authenticated Alpha profile in the client instead.
create or replace function public.get_public_ranks(uuids uuid[])
returns table (mc_uuid uuid, rank text)
language sql
stable
security definer
set search_path = ''
as $$
  select distinct on (requested.mc_uuid)
         requested.mc_uuid,
         case
           when coalesce(p.is_founder, false) or p.rank_key = 'founder' then 'founder'
           else coalesce(nullif(p.rank_key, ''), 'member')
         end as rank
  from unnest(coalesce(uuids[1:100], array[]::uuid[])) as requested(mc_uuid)
  join public.alpha_minecraft_accounts a on a.mc_uuid = requested.mc_uuid
  join public.profiles p on p.id = a.profile_id
  order by requested.mc_uuid, a.linked_at desc, a.profile_id;
$$;

revoke all on function public.get_public_ranks(uuid[]) from public;
grant execute on function public.get_public_ranks(uuid[]) to anon, authenticated;

commit;
