-- Central command permissions managed from AlphaClient and shared through Supabase.
create table if not exists public.client_command_permission_catalog (
    permission_key text primary key,
    display_name text not null,
    category text not null,
    description text not null default '',
    created_at timestamptz not null default now(),
    constraint client_command_permission_key_format check (permission_key ~ '^[a-z][a-z0-9_.-]*$')
);

create table if not exists public.client_command_rank_permissions (
    rank_key text not null references public.community_ranks(rank_key) on delete cascade,
    permission_key text not null references public.client_command_permission_catalog(permission_key) on delete cascade,
    enabled boolean not null default true,
    updated_at timestamptz not null default now(),
    updated_by uuid references auth.users(id) on delete set null,
    primary key (rank_key, permission_key)
);

create table if not exists public.client_command_user_overrides (
    profile_id uuid not null references public.profiles(id) on delete cascade,
    permission_key text not null references public.client_command_permission_catalog(permission_key) on delete cascade,
    effect text not null check (effect in ('allow', 'deny')),
    updated_at timestamptz not null default now(),
    updated_by uuid references auth.users(id) on delete set null,
    primary key (profile_id, permission_key)
);

alter table public.client_command_permission_catalog enable row level security;
alter table public.client_command_rank_permissions enable row level security;
alter table public.client_command_user_overrides enable row level security;

revoke all on public.client_command_permission_catalog from anon, authenticated;
revoke all on public.client_command_rank_permissions from anon, authenticated;
revoke all on public.client_command_user_overrides from anon, authenticated;

insert into public.client_command_permission_catalog (permission_key, display_name, category, description) values
('command.help','AlphaClient Help','General','View AlphaClient command help'),
('command.modules','Module List','General','List available modules'),
('command.settings','Module Settings','General','Open module settings'),
('command.hudeditor','HUD Editor','General','Open the HUD editor'),
('command.profiles.list','List Profiles','Profiles','List available profiles'),
('command.profiles.switch','Switch Profiles','Profiles','Switch active profile'),
('command.profiles.create','Create Profiles','Profiles','Create a profile'),
('command.profiles.edit','Rename Profiles','Profiles','Rename a profile'),
('command.profiles.delete','Delete Profiles','Profiles','Delete a profile'),
('command.fpshud','FPS HUD','HUD & Modules','Use FPS HUD commands'),
('command.cpshud','CPS HUD','HUD & Modules','Use CPS HUD commands'),
('command.bhop','BHOP','HUD & Modules','Use BHOP commands and settings'),
('command.wtap','W-Tap','HUD & Modules','Use W-Tap commands and settings'),
('command.fullbright','Fullbright','HUD & Modules','Use Fullbright commands'),
('command.nameviewer','Name Viewer','HUD & Modules','Use Name Viewer commands'),
('command.legacypvp','Legacy PvP','HUD & Modules','Use Legacy PvP commands'),
('command.playtime','Playtime Tracker','HUD & Modules','Use Playtime Tracker commands'),
('command.sessiontimer','Session Timer','HUD & Modules','Use Session Timer commands'),
('command.afktimer','AFK Timer','HUD & Modules','Use AFK Timer commands'),
('command.quality','Quality Settings','HUD & Modules','Open Quality settings'),
('menu.general','General Menu','Menus','Open Mods > General'),
('menu.beta','Beta Menu','Menus','Open Mods > Beta'),
('menu.donator','Donator Menu','Menus','Open Mods > Donator'),
('menu.scout','Scout Menu','Menus','Open Mods > Scout'),
('menu.rogue','Rogue Menu','Menus','Open Mods > Rogue'),
('menu.elite','Elite Menu','Menus','Open Mods > Elite'),
('menu.alpha','Alpha Menu','Menus','Open Mods > Alpha'),
('menu.alphaplus','Alpha+ Menu','Menus','Open Mods > Alpha+'),
('staffmods.open','Open StaffMods','StaffMods','Open the StaffMods menu'),
('staffmods.general','StaffMods General','StaffMods','Open StaffMods > General'),
('staffmods.tmod','Trial Moderator Folder','StaffMods','Open the Trial Moderator folder'),
('staffmods.mod','Moderator Folder','StaffMods','Open the Moderator folder'),
('staffmods.srmod','Senior Moderator Folder','StaffMods','Open the Senior Moderator folder'),
('staffmods.admin','Admin Folder','StaffMods','Open the Admin folder'),
('staffmods.sradmin','Senior Admin Folder','StaffMods','Open the Senior Admin folder'),
('staffmods.platformadmin','Platform Admin Folder','StaffMods','Open the Platform Admin folder'),
('staffmods.management','Management Folder','StaffMods','Open the shared Management folder'),
('staffmods.manager','Manager Folder','StaffMods','Open the Manager folder'),
('staffmods.owner','Owner Folder','StaffMods','Open the Owner folder'),
('staffmods.founder','Founder Folder','StaffMods','Open the private Founder folder')
on conflict (permission_key) do update set
 display_name = excluded.display_name,
 category = excluded.category,
 description = excluded.description;

create index if not exists idx_client_command_rank_permissions_key
    on public.client_command_rank_permissions(permission_key);
create index if not exists idx_client_command_user_overrides_key
    on public.client_command_user_overrides(permission_key);

comment on table public.client_command_permission_catalog is 'Allowlisted AlphaClient command permissions.';
comment on table public.client_command_rank_permissions is 'Rank-level AlphaClient command permissions; writes are server-side only.';
comment on table public.client_command_user_overrides is 'Per-account command permission overrides; account overrides take precedence over rank permissions.';
