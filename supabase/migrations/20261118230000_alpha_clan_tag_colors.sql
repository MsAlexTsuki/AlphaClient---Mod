begin;

alter table public.clans
    add column if not exists tag_color text not null default 'yellow';

do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conrelid = 'public.clans'::regclass
          and conname = 'clans_tag_color_allowed'
    ) then
        alter table public.clans
            add constraint clans_tag_color_allowed
            check (tag_color in (
                'black','dark_blue','dark_green','dark_aqua',
                'dark_red','dark_purple','gold','gray',
                'dark_gray','blue','green','aqua',
                'red','light_purple','yellow','white'
            ));
    end if;
end;
$$;

create or replace function public.get_public_clan_tag_styles(uuids uuid[])
returns table(mc_uuid uuid, clan_tag text, tag_color text)
language sql
stable
security definer
set search_path = ''
as $$
    select distinct on (requested.mc_uuid)
        requested.mc_uuid,
        c.tag,
        c.tag_color
    from unnest(coalesce(uuids[1:100], array[]::uuid[])) as requested(mc_uuid)
    join public.alpha_minecraft_accounts a on a.mc_uuid = requested.mc_uuid
    join public.clan_members m on m.profile_id = a.profile_id
    join public.clans c on c.id = m.clan_id
    order by requested.mc_uuid, a.linked_at desc, a.profile_id;
$$;

create or replace function public.set_clan_tag_color(p_tag_color text)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
    clean_color text := lower(btrim(coalesce(p_tag_color, '')));
begin
    if auth.uid() is null then
        raise exception 'Authentication required.' using errcode = '42501';
    end if;

    if clean_color not in (
        'black','dark_blue','dark_green','dark_aqua',
        'dark_red','dark_purple','gold','gray',
        'dark_gray','blue','green','aqua',
        'red','light_purple','yellow','white'
    ) then
        raise exception 'Choose one of the standard Minecraft colors.' using errcode = '22023';
    end if;

    update public.clans c
       set tag_color = clean_color,
           updated_at = now()
      from public.clan_members m
     where m.clan_id = c.id
       and m.profile_id = auth.uid()
       and m.role = 'owner';

    if not found then
        raise exception 'Only the clan owner can change the clan tag color.' using errcode = '42501';
    end if;

    return true;
end;
$$;

revoke all on function public.get_public_clan_tag_styles(uuid[]) from public;
grant execute on function public.get_public_clan_tag_styles(uuid[]) to anon, authenticated;

revoke all on function public.set_clan_tag_color(text) from public, anon;
grant execute on function public.set_clan_tag_color(text) to authenticated;

comment on column public.clans.tag_color is
    'The clan-wide Minecraft chat formatting color used for the clan tag above member nametags.';

commit;
