-- Pixel Garden root permission and its five canonical child permissions.
-- The runtime expands a granted "pixelgarden" ID to all five child IDs.
insert into public.client_command_permission_catalog
    (permission_key, display_name, category, description)
values
    ('pixelgarden', 'Pixel Garden', 'Founder Fun', 'Access to all Pixel Garden features'),
    ('menu.pixelgarden', 'Pixel Garden Menu', 'Founder Fun', 'Open the Pixel Garden menu'),
    ('commands.pixelgarden', 'Pixel Garden Commands', 'Founder Fun', 'Use Pixel Garden commands'),
    ('pixelgarden.shop', 'Pixel Garden Shop', 'Founder Fun', 'Access the Pixel Garden shop'),
    ('pixelgarden.upgrades', 'Pixel Garden Upgrades', 'Founder Fun', 'Use Pixel Garden upgrades'),
    ('pixelgarden.settings', 'Pixel Garden Settings', 'Founder Fun', 'Manage Pixel Garden settings')
on conflict (permission_key) do update set
    display_name = excluded.display_name,
    category = excluded.category,
    description = excluded.description;
