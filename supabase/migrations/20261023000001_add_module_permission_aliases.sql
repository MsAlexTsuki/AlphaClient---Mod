insert into public.client_command_permission_catalog (permission_key, display_name, category, description) values
('mod.staff.bhop','BHOP Module Access','Modules','Unlock the BHOP module entitlement (used with command.bhop)'),
('mod.w-tap-assistance','W-Tap Module Access','Modules','Unlock W-Tap Assistance (used with command.wtap)'),
('mod.legacy-pvp','Legacy PvP Module Access','Modules','Unlock the Legacy PvP module'),
('mod.name-viewer','Name Viewer Module Access','Modules','Unlock the Name Viewer module'),
('mod.public.custom_hud','CPS Module Access','Modules','Unlock the CPS HUD module'),
('mod.public.performance_monitor','FPS Module Access','Modules','Unlock the FPS HUD module'),
('mod.quality','Quality Module Access','Modules','Unlock Quality module access')
on conflict (permission_key) do update set
 display_name = excluded.display_name,
 category = excluded.category,
 description = excluded.description;