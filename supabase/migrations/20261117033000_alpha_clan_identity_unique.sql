begin;

-- Clan names are unique regardless of capitalization or surrounding whitespace.
create unique index if not exists clans_name_normalized_uidx
    on public.clans (lower(btrim(name)));

-- Tags are unique regardless of capitalization or surrounding whitespace.
create unique index if not exists clans_tag_normalized_uidx
    on public.clans (upper(btrim(tag)));

-- Keep the database rules aligned with the AlphaClient clan-tag requirement.
alter table public.clans drop constraint if exists clans_tag_check;
alter table public.clans
    add constraint clans_tag_check
    check (tag ~ '^[A-Z]{2,3}$');

commit;
