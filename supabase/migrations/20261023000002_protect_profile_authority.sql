-- Keep client-editable profile fields writable without allowing self-promotion.
revoke insert, update, delete on public.profiles from anon, authenticated;
grant update (username, display_name, bio, avatar_url) on public.profiles to authenticated;

comment on column public.profiles.is_founder is 'Server-managed Founder flag; never writable by public client roles.';
comment on column public.profiles.rank_key is 'Server-managed primary community rank; never writable by public client roles.';
