create table if not exists public.client_easter_egg_settings (
  singleton boolean primary key default true check (singleton),
  globally_enabled boolean not null default true,
  master_enabled jsonb not null default '{}'::jsonb check (jsonb_typeof(master_enabled) = 'object'),
  updated_at timestamptz not null default now(),
  updated_by uuid references auth.users(id)
);

alter table public.client_easter_egg_settings enable row level security;
revoke all on public.client_easter_egg_settings from anon, authenticated;
grant all on public.client_easter_egg_settings to service_role;

insert into public.client_easter_egg_settings (singleton, globally_enabled, master_enabled)
values (true, true, jsonb_build_object('1',true,'2',true,'3',true,'4',true,'5',true,'6',true,'7',true,'8',true,'9',true,'10',true,'11',true,'12',true,'13',true,'14',true,'15',true,'16',true,'17',true,'18',true,'19',true,'20',true,'21',true,'22',true,'23',true,'24',true,'25',true))
on conflict (singleton) do nothing;
