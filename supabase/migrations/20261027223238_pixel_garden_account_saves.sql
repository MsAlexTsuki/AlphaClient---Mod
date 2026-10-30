-- Pixel Garden saves are owned by the authenticated Alpha community account.
create table if not exists public.pixel_garden_saves (
  user_id uuid primary key references auth.users(id) on delete cascade,
  save_data jsonb not null default '{}'::jsonb,
  saved_at timestamptz not null default now(),
  constraint pixel_garden_save_object check (jsonb_typeof(save_data) = 'object')
);
alter table public.pixel_garden_saves enable row level security;
drop policy if exists "Users can read their own Pixel Garden save" on public.pixel_garden_saves;
create policy "Users can read their own Pixel Garden save"
  on public.pixel_garden_saves for select to authenticated
  using ((select auth.uid()) = user_id);
drop policy if exists "Users can insert their own Pixel Garden save" on public.pixel_garden_saves;
create policy "Users can insert their own Pixel Garden save"
  on public.pixel_garden_saves for insert to authenticated
  with check ((select auth.uid()) = user_id);
drop policy if exists "Users can update their own Pixel Garden save" on public.pixel_garden_saves;
create policy "Users can update their own Pixel Garden save"
  on public.pixel_garden_saves for update to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);
grant select, insert, update on public.pixel_garden_saves to authenticated;
