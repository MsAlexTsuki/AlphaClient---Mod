
begin;
create table if not exists public.clans (
 id uuid primary key default gen_random_uuid(),
 name text not null check (char_length(btrim(name)) between 3 and 24),
 tag text not null unique check (tag ~ '^[A-Z0-9]{2,5}$'),
 description text not null default '' check (char_length(description) <= 160),
 visibility text not null default 'public' check (visibility in ('public','private')),
 owner_id uuid not null references public.profiles(id) on delete cascade,
 created_at timestamptz not null default now(),
 updated_at timestamptz not null default now()
);
create table if not exists public.clan_members (
 clan_id uuid not null references public.clans(id) on delete cascade,
 profile_id uuid not null references public.profiles(id) on delete cascade,
 role text not null default 'member' check (role in ('owner','officer','member')),
 joined_at timestamptz not null default now(),
 primary key (clan_id, profile_id),
 unique (profile_id)
);
create table if not exists public.clan_invites (
 id uuid primary key default gen_random_uuid(),
 clan_id uuid not null references public.clans(id) on delete cascade,
 inviter_id uuid not null references public.profiles(id) on delete cascade,
 target_profile_id uuid not null references public.profiles(id) on delete cascade,
 created_at timestamptz not null default now(),
 expires_at timestamptz not null default (now() + interval '7 days'),
 unique (clan_id, target_profile_id)
);
create table if not exists public.clan_join_requests (
 id uuid primary key default gen_random_uuid(),
 clan_id uuid not null references public.clans(id) on delete cascade,
 profile_id uuid not null references public.profiles(id) on delete cascade,
 status text not null default 'pending' check (status in ('pending','accepted','declined','cancelled')),
 created_at timestamptz not null default now(),
 responded_at timestamptz
);
create unique index if not exists clan_join_requests_one_pending_idx
 on public.clan_join_requests(clan_id,profile_id) where status='pending';
create table if not exists public.clan_messages (
 id uuid primary key default gen_random_uuid(),
 clan_id uuid not null references public.clans(id) on delete cascade,
 sender_id uuid not null references public.profiles(id) on delete cascade,
 body text not null check (char_length(btrim(body)) between 1 and 256),
 created_at timestamptz not null default now()
);
create index if not exists clan_members_clan_id_idx on public.clan_members(clan_id);
create index if not exists clan_invites_target_idx on public.clan_invites(target_profile_id,expires_at);
create index if not exists clan_join_requests_clan_status_idx on public.clan_join_requests(clan_id,status,created_at desc);
create index if not exists clan_messages_clan_created_idx on public.clan_messages(clan_id,created_at desc);
alter table public.clans enable row level security;
alter table public.clan_members enable row level security;
alter table public.clan_invites enable row level security;
alter table public.clan_join_requests enable row level security;
alter table public.clan_messages enable row level security;

create or replace function public.is_clan_member(target_clan_id uuid)
returns boolean language sql stable security definer set search_path=''
as $$ select auth.uid() is not null and exists (
 select 1 from public.clan_members m where m.clan_id=target_clan_id and m.profile_id=auth.uid()
); $$;
create or replace function public.is_clan_manager(target_clan_id uuid)
returns boolean language sql stable security definer set search_path=''
as $$ select auth.uid() is not null and exists (
 select 1 from public.clan_members m where m.clan_id=target_clan_id and m.profile_id=auth.uid() and m.role in ('owner','officer')
); $$;

drop policy if exists clans_member_read on public.clans;
create policy clans_member_read on public.clans for select to authenticated using (public.is_clan_member(id));
drop policy if exists clan_members_member_read on public.clan_members;
create policy clan_members_member_read on public.clan_members for select to authenticated using (public.is_clan_member(clan_id));
drop policy if exists clan_invites_recipient_or_manager_read on public.clan_invites;
create policy clan_invites_recipient_or_manager_read on public.clan_invites for select to authenticated using (target_profile_id=(select auth.uid()) or public.is_clan_manager(clan_id));
drop policy if exists clan_join_requests_requester_or_manager_read on public.clan_join_requests;
create policy clan_join_requests_requester_or_manager_read on public.clan_join_requests for select to authenticated using (profile_id=(select auth.uid()) or public.is_clan_manager(clan_id));
drop policy if exists clan_messages_member_read on public.clan_messages;
create policy clan_messages_member_read on public.clan_messages for select to authenticated using (public.is_clan_member(clan_id));
drop policy if exists clan_messages_member_insert on public.clan_messages;
create policy clan_messages_member_insert on public.clan_messages for insert to authenticated with check (sender_id=(select auth.uid()) and public.is_clan_member(clan_id));

create or replace function public.create_clan(p_name text,p_tag text,p_description text default '',p_visibility text default 'public')
returns uuid language plpgsql security definer set search_path=''
as $$
declare new_clan_id uuid; clean_name text:=btrim(coalesce(p_name,'')); clean_tag text:=upper(btrim(coalesce(p_tag,'')));
begin
 if auth.uid() is null then raise exception 'Sign in to create a clan.' using errcode='42501'; end if;
 if exists(select 1 from public.clan_members where profile_id=auth.uid()) then raise exception 'You are already in a clan.' using errcode='23505'; end if;
 if char_length(clean_name) not between 3 and 24 then raise exception 'Clan names must contain 3 to 24 characters.' using errcode='22023'; end if;
 if clean_tag !~ '^[A-Z0-9]{2,5}$' then raise exception 'Clan tags must contain 2 to 5 letters or numbers.' using errcode='22023'; end if;
 if coalesce(p_visibility,'public') not in ('public','private') then raise exception 'Choose public or private visibility.' using errcode='22023'; end if;
 insert into public.clans(name,tag,description,visibility,owner_id) values(clean_name,clean_tag,left(coalesce(p_description,''),160),coalesce(p_visibility,'public'),auth.uid()) returning id into new_clan_id;
 insert into public.clan_members(clan_id,profile_id,role) values(new_clan_id,auth.uid(),'owner');
 return new_clan_id;
end; $$;

create or replace function public.update_clan_settings(p_clan_id uuid,p_name text,p_tag text,p_description text,p_visibility text)
returns boolean language plpgsql security definer set search_path=''
as $$
declare clean_name text:=btrim(coalesce(p_name,'')); clean_tag text:=upper(btrim(coalesce(p_tag,'')));
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 if not exists(select 1 from public.clan_members where clan_id=p_clan_id and profile_id=auth.uid() and role='owner') then raise exception 'Only the clan owner can edit clan settings.' using errcode='42501'; end if;
 if char_length(clean_name) not between 3 and 24 or clean_tag !~ '^[A-Z0-9]{2,5}$' or coalesce(p_visibility,'') not in ('public','private') then raise exception 'Invalid clan settings.' using errcode='22023'; end if;
 update public.clans set name=clean_name,tag=clean_tag,description=left(coalesce(p_description,''),160),visibility=p_visibility,updated_at=now() where id=p_clan_id;
 return true;
end; $$;

create or replace function public.join_public_clan(p_clan_id uuid)
returns boolean language plpgsql security definer set search_path=''
as $$
declare target_visibility text; member_count integer;
begin
 if auth.uid() is null then raise exception 'Sign in to join a clan.' using errcode='42501'; end if;
 if exists(select 1 from public.clan_members where profile_id=auth.uid()) then raise exception 'You are already in a clan.' using errcode='23505'; end if;
 select visibility into target_visibility from public.clans where id=p_clan_id for update;
 if target_visibility is null then raise exception 'Clan not found.' using errcode='P0002'; end if;
 if target_visibility<>'public' then raise exception 'This is a private clan. Send a join request instead.' using errcode='42501'; end if;
 select count(*) into member_count from public.clan_members where clan_id=p_clan_id;
 if member_count>=50 then raise exception 'This clan has reached its 50-member limit.' using errcode='23514'; end if;
 insert into public.clan_members(clan_id,profile_id,role) values(p_clan_id,auth.uid(),'member');
 return true;
end; $$;

create or replace function public.request_to_join_clan(p_clan_id uuid)
returns uuid language plpgsql security definer set search_path=''
as $$
declare target_visibility text; request_id uuid;
begin
 if auth.uid() is null then raise exception 'Sign in to request to join a clan.' using errcode='42501'; end if;
 if exists(select 1 from public.clan_members where profile_id=auth.uid()) then raise exception 'You are already in a clan.' using errcode='23505'; end if;
 select visibility into target_visibility from public.clans where id=p_clan_id;
 if target_visibility is null then raise exception 'Clan not found.' using errcode='P0002'; end if;
 if target_visibility<>'private' then raise exception 'Public clans can be joined directly.' using errcode='22023'; end if;
 insert into public.clan_join_requests(clan_id,profile_id) values(p_clan_id,auth.uid()) returning id into request_id;
 return request_id;
exception when unique_violation then raise exception 'You already have a pending request for this clan.' using errcode='23505';
end; $$;

create or replace function public.respond_to_clan_join_request(p_request_id uuid,p_accept boolean)
returns boolean language plpgsql security definer set search_path=''
as $$
declare request_row public.clan_join_requests%rowtype; member_count integer;
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 select r.* into request_row from public.clan_join_requests r where r.id=p_request_id and r.status='pending' for update;
 if request_row.id is null then raise exception 'Pending join request not found.' using errcode='P0002'; end if;
 if not exists(select 1 from public.clan_members where clan_id=request_row.clan_id and profile_id=auth.uid() and role in ('owner','officer')) then raise exception 'Only the clan owner or an officer can respond to join requests.' using errcode='42501'; end if;
 if p_accept then
  if exists(select 1 from public.clan_members where profile_id=request_row.profile_id) then raise exception 'This player is already in a clan.' using errcode='23505'; end if;
  perform 1 from public.clans where id=request_row.clan_id for update;
  select count(*) into member_count from public.clan_members where clan_id=request_row.clan_id;
  if member_count>=50 then raise exception 'This clan has reached its 50-member limit.' using errcode='23514'; end if;
  insert into public.clan_members(clan_id,profile_id,role) values(request_row.clan_id,request_row.profile_id,'member');
 end if;
 update public.clan_join_requests set status=case when p_accept then 'accepted' else 'declined' end,responded_at=now() where id=p_request_id;
 return true;
end; $$;

create or replace function public.invite_clan_member(p_clan_id uuid,p_username text)
returns uuid language plpgsql security definer set search_path=''
as $$
declare target_profile_id uuid; invite_id uuid; member_count integer;
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 if not exists(select 1 from public.clan_members where clan_id=p_clan_id and profile_id=auth.uid() and role in ('owner','officer')) then raise exception 'Only the clan owner or an officer can invite players.' using errcode='42501'; end if;
 select id into target_profile_id from public.profiles where username=lower(btrim(coalesce(p_username,'')));
 if target_profile_id is null then raise exception 'Alpha player not found.' using errcode='P0002'; end if;
 if target_profile_id=auth.uid() then raise exception 'You cannot invite yourself.' using errcode='22023'; end if;
 if exists(select 1 from public.clan_members where profile_id=target_profile_id) then raise exception 'That player is already in a clan.' using errcode='23505'; end if;
 perform 1 from public.clans where id=p_clan_id for update;
 select count(*) into member_count from public.clan_members where clan_id=p_clan_id;
 if member_count>=50 then raise exception 'This clan has reached its 50-member limit.' using errcode='23514'; end if;
 insert into public.clan_invites(clan_id,inviter_id,target_profile_id) values(p_clan_id,auth.uid(),target_profile_id)
 on conflict(clan_id,target_profile_id) do update set inviter_id=excluded.inviter_id,created_at=now(),expires_at=now()+interval '7 days' returning id into invite_id;
 return invite_id;
end; $$;

create or replace function public.respond_to_clan_invite(p_invite_id uuid,p_accept boolean)
returns boolean language plpgsql security definer set search_path=''
as $$
declare invite_row public.clan_invites%rowtype; member_count integer;
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 select i.* into invite_row from public.clan_invites i where i.id=p_invite_id and i.target_profile_id=auth.uid() and i.expires_at>now() for update;
 if invite_row.id is null then raise exception 'Valid clan invite not found.' using errcode='P0002'; end if;
 if p_accept then
  if exists(select 1 from public.clan_members where profile_id=auth.uid()) then raise exception 'You are already in a clan.' using errcode='23505'; end if;
  perform 1 from public.clans where id=invite_row.clan_id for update;
  select count(*) into member_count from public.clan_members where clan_id=invite_row.clan_id;
  if member_count>=50 then raise exception 'This clan has reached its 50-member limit.' using errcode='23514'; end if;
  insert into public.clan_members(clan_id,profile_id,role) values(invite_row.clan_id,auth.uid(),'member');
 end if;
 delete from public.clan_invites where id=p_invite_id;
 return true;
end; $$;

create or replace function public.leave_clan()
returns boolean language plpgsql security definer set search_path=''
as $$
declare member_row public.clan_members%rowtype; member_count integer;
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 select * into member_row from public.clan_members where profile_id=auth.uid() for update;
 if member_row.profile_id is null then raise exception 'You are not in a clan.' using errcode='P0002'; end if;
 if member_row.role='owner' then
  select count(*) into member_count from public.clan_members where clan_id=member_row.clan_id;
  if member_count>1 then raise exception 'Transfer ownership or disband the clan before leaving.' using errcode='23514'; end if;
  delete from public.clans where id=member_row.clan_id;
 else
  delete from public.clan_members where clan_id=member_row.clan_id and profile_id=auth.uid();
 end if;
 return true;
end; $$;

create or replace function public.remove_clan_member(p_target_profile_id uuid)
returns boolean language plpgsql security definer set search_path=''
as $$
declare actor_role text; target_role text; target_clan_id uuid;
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 select clan_id,role into target_clan_id,actor_role from public.clan_members where profile_id=auth.uid();
 if target_clan_id is null or actor_role not in ('owner','officer') then raise exception 'You cannot remove clan members.' using errcode='42501'; end if;
 select role into target_role from public.clan_members where clan_id=target_clan_id and profile_id=p_target_profile_id;
 if target_role is null or target_role='owner' then raise exception 'That player cannot be removed.' using errcode='22023'; end if;
 if target_role='officer' and actor_role<>'owner' then raise exception 'Only the owner can remove an officer.' using errcode='42501'; end if;
 delete from public.clan_members where clan_id=target_clan_id and profile_id=p_target_profile_id;
 return true;
end; $$;

create or replace function public.set_clan_member_role(p_target_profile_id uuid,p_role text)
returns boolean language plpgsql security definer set search_path=''
as $$
declare target_clan_id uuid;
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 select clan_id into target_clan_id from public.clan_members where profile_id=auth.uid() and role='owner';
 if target_clan_id is null then raise exception 'Only the owner can change member roles.' using errcode='42501'; end if;
 if p_role not in ('officer','member') then raise exception 'Role must be officer or member.' using errcode='22023'; end if;
 update public.clan_members set role=p_role where clan_id=target_clan_id and profile_id=p_target_profile_id and role<>'owner';
 if not found then raise exception 'Clan member not found.' using errcode='P0002'; end if;
 return true;
end; $$;

create or replace function public.transfer_clan_ownership(p_target_profile_id uuid)
returns boolean language plpgsql security definer set search_path=''
as $$
declare target_clan_id uuid;
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 select clan_id into target_clan_id from public.clan_members where profile_id=auth.uid() and role='owner' for update;
 if target_clan_id is null then raise exception 'Only the owner can transfer ownership.' using errcode='42501'; end if;
 if not exists(select 1 from public.clan_members where clan_id=target_clan_id and profile_id=p_target_profile_id and profile_id<>auth.uid()) then raise exception 'Choose another clan member.' using errcode='22023'; end if;
 update public.clan_members set role='officer' where clan_id=target_clan_id and profile_id=auth.uid();
 update public.clan_members set role='owner' where clan_id=target_clan_id and profile_id=p_target_profile_id;
 update public.clans set owner_id=p_target_profile_id,updated_at=now() where id=target_clan_id;
 return true;
end; $$;

create or replace function public.disband_clan(p_clan_id uuid)
returns boolean language plpgsql security definer set search_path=''
as $$
begin
 if auth.uid() is null then raise exception 'Authentication required.' using errcode='42501'; end if;
 if not exists(select 1 from public.clan_members where clan_id=p_clan_id and profile_id=auth.uid() and role='owner') then raise exception 'Only the owner can disband the clan.' using errcode='42501'; end if;
 delete from public.clans where id=p_clan_id;
 return true;
end; $$;

create or replace function public.get_clan_browser()
returns table(id uuid,name text,tag text,description text,visibility text,member_count bigint,created_at timestamptz)
language sql stable security definer set search_path=''
as $$
 select c.id,c.name,c.tag,c.description,c.visibility,count(m.profile_id),c.created_at
 from public.clans c left join public.clan_members m on m.clan_id=c.id
 group by c.id order by c.name asc;
$$;

create or replace function public.get_public_clan_tags(uuids uuid[])
returns table(mc_uuid uuid,clan_tag text,clan_name text)
language sql stable security definer set search_path=''
as $$
 select distinct on(requested.mc_uuid) requested.mc_uuid,c.tag,c.name
 from unnest(coalesce(uuids[1:100],array[]::uuid[])) as requested(mc_uuid)
 join public.alpha_minecraft_accounts a on a.mc_uuid=requested.mc_uuid
 join public.clan_members m on m.profile_id=a.profile_id
 join public.clans c on c.id=m.clan_id
 order by requested.mc_uuid,a.linked_at desc,a.profile_id;
$$;

create or replace function public.broadcast_clan_message()
returns trigger language plpgsql security definer set search_path=''
as $$
begin
 perform realtime.broadcast_changes('clan:'||new.clan_id::text,'INSERT',tg_op,tg_table_name,tg_table_schema,new,old);
 return new;
end; $$;
drop trigger if exists clan_messages_broadcast on public.clan_messages;
create trigger clan_messages_broadcast after insert on public.clan_messages
 for each row execute function public.broadcast_clan_message();

drop policy if exists clan_chat_private_channel_read on realtime.messages;
create policy clan_chat_private_channel_read on realtime.messages for select to authenticated
 using(extension='broadcast' and (select realtime.topic()) ~ '^clan:[0-9a-fA-F-]{36}$'
 and public.is_clan_member(split_part((select realtime.topic()),':',2)::uuid));

grant select on public.clans,public.clan_members,public.clan_invites,public.clan_join_requests,public.clan_messages to authenticated;
grant insert on public.clan_messages to authenticated;
grant execute on function public.is_clan_member(uuid) to authenticated;
grant execute on function public.is_clan_manager(uuid) to authenticated;
grant execute on function public.create_clan(text,text,text,text) to authenticated;
grant execute on function public.update_clan_settings(uuid,text,text,text,text) to authenticated;
grant execute on function public.join_public_clan(uuid) to authenticated;
grant execute on function public.request_to_join_clan(uuid) to authenticated;
grant execute on function public.respond_to_clan_join_request(uuid,boolean) to authenticated;
grant execute on function public.invite_clan_member(uuid,text) to authenticated;
grant execute on function public.respond_to_clan_invite(uuid,boolean) to authenticated;
grant execute on function public.leave_clan() to authenticated;
grant execute on function public.remove_clan_member(uuid) to authenticated;
grant execute on function public.set_clan_member_role(uuid,text) to authenticated;
grant execute on function public.transfer_clan_ownership(uuid) to authenticated;
grant execute on function public.disband_clan(uuid) to authenticated;
grant execute on function public.get_clan_browser() to anon,authenticated;
grant execute on function public.get_public_clan_tags(uuid[]) to anon,authenticated;

revoke all on function public.create_clan(text,text,text,text) from public,anon;
revoke all on function public.update_clan_settings(uuid,text,text,text,text) from public,anon;
revoke all on function public.join_public_clan(uuid) from public,anon;
revoke all on function public.request_to_join_clan(uuid) from public,anon;
revoke all on function public.respond_to_clan_join_request(uuid,boolean) from public,anon;
revoke all on function public.invite_clan_member(uuid,text) from public,anon;
revoke all on function public.respond_to_clan_invite(uuid,boolean) from public,anon;
revoke all on function public.leave_clan() from public,anon;
revoke all on function public.remove_clan_member(uuid) from public,anon;
revoke all on function public.set_clan_member_role(uuid,text) from public,anon;
revoke all on function public.transfer_clan_ownership(uuid) from public,anon;
revoke all on function public.disband_clan(uuid) from public,anon;
revoke all on function public.get_clan_browser() from public;
revoke all on function public.get_public_clan_tags(uuid[]) from public;
grant execute on function public.get_clan_browser() to anon,authenticated;
grant execute on function public.get_public_clan_tags(uuid[]) to anon,authenticated;
commit;
