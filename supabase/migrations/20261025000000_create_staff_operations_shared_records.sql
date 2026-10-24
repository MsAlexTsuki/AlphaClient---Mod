-- Shared live data for AlphaClient Staff Operations.
-- The matching migration is applied to Supabase project lescvpixttmmbnaluidd.
create table if not exists public.staff_operations_records (
  id uuid primary key default gen_random_uuid(),
  record_type text not null check (record_type in ('case','shift','presence','audit')),
  title text not null default '',
  status text not null default 'open',
  payload jsonb not null default '{}'::jsonb,
  created_by uuid not null references auth.users(id),
  assigned_to uuid references auth.users(id),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index if not exists staff_operations_records_type_updated_idx
  on public.staff_operations_records (record_type, updated_at desc);
create index if not exists staff_operations_records_assigned_idx
  on public.staff_operations_records (assigned_to, updated_at desc);
alter table public.staff_operations_records enable row level security;
revoke all on public.staff_operations_records from anon, authenticated;
grant select, insert, update on public.staff_operations_records to authenticated;
create or replace function public.staff_operations_is_staff()
returns boolean language sql stable security definer set search_path = public as $$
  select exists (select 1 from public.profiles p where p.id = auth.uid()
    and (p.is_founder or p.rank_key in ('trial_moderator','moderator','senior_moderator','admin','senior_admin','plat_admin','manager','owner','founder')));
$$;
drop policy if exists staff_operations_staff_read on public.staff_operations_records;
create policy staff_operations_staff_read on public.staff_operations_records for select to authenticated using (public.staff_operations_is_staff());
drop policy if exists staff_operations_staff_insert on public.staff_operations_records;
create policy staff_operations_staff_insert on public.staff_operations_records for insert to authenticated with check (public.staff_operations_is_staff() and created_by = auth.uid() and record_type <> 'audit');
drop policy if exists staff_operations_staff_update on public.staff_operations_records;
create policy staff_operations_staff_update on public.staff_operations_records for update to authenticated using (public.staff_operations_is_staff() and record_type <> 'audit') with check (public.staff_operations_is_staff() and record_type <> 'audit');
create or replace function public.staff_operations_touch_updated_at()
returns trigger language plpgsql set search_path = public as $$
begin new.updated_at = now(); return new; end; $$;
drop trigger if exists staff_operations_touch_updated_at on public.staff_operations_records;
create trigger staff_operations_touch_updated_at before update on public.staff_operations_records for each row execute function public.staff_operations_touch_updated_at();
