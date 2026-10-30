-- Pixel Garden is a shared Donator feature, visible in the Scout folder.
-- Grant the root permission to every Donator tier; runtime permission expansion
-- supplies menu.pixelgarden, commands.pixelgarden, pixelgarden.shop,
-- pixelgarden.upgrades, and pixelgarden.settings.
insert into public.client_command_rank_permissions (rank_key, permission_key, enabled)
select ranks.rank_key, 'pixelgarden', true
from public.community_ranks ranks
where ranks.rank_key in ('scout', 'rogue', 'elite', 'alpha', 'alpha_plus')
  and exists (
      select 1
      from public.client_command_permission_catalog catalog
      where catalog.permission_key = 'pixelgarden'
  )
on conflict (rank_key, permission_key) do update
set enabled = excluded.enabled, updated_at = now();
