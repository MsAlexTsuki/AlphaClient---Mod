create table if not exists public.pixel_garden_quest_catalog (
  quest_id text primary key check (quest_id ~ '^[a-z][a-z0-9_]{2,63}$'),
  definition jsonb not null check (jsonb_typeof(definition) = 'object'),
  created_by uuid not null references auth.users(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.pixel_garden_quest_catalog enable row level security;
revoke all on public.pixel_garden_quest_catalog from anon, authenticated;

comment on table public.pixel_garden_quest_catalog is
  'Shared Pixel Garden quest definitions. Access is mediated by the pixel-garden-quests Edge Function.';
