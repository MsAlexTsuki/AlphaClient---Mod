-- Enforce private staff note edit/delete permissions in PostgreSQL, not only in the client.
create or replace function public.staff_private_note_can_delete(p_note_id uuid)
returns boolean
language sql stable security definer
set search_path = ''
as $$
  select exists (
    select 1
    from public.staff_private_notes n
    join public.profiles author on author.id = n.created_by
    join public.profiles actor on actor.id = auth.uid()
    where n.id = p_note_id
      and (actor.is_founder or actor.rank_key in ('manager','owner','founder'))
      and (
        (n.created_by = auth.uid() and not author.is_founder)
        or (actor.is_founder)
        or (actor.rank_key = 'owner' and not actor.is_founder and author.rank_key = 'manager' and not author.is_founder)
      )
  );
$$;
revoke all on function public.staff_private_note_can_delete(uuid) from public, anon;
grant execute on function public.staff_private_note_can_delete(uuid) to authenticated;

drop policy if exists staff_private_notes_update on public.staff_private_notes;
create policy staff_private_notes_update on public.staff_private_notes
  for update to authenticated
  using (
    public.staff_operations_is_manager_or_higher()
    and created_by = auth.uid()
    and exists (select 1 from public.profiles p where p.id = auth.uid() and not p.is_founder)
  )
  with check (
    public.staff_operations_is_manager_or_higher()
    and created_by = auth.uid()
    and exists (select 1 from public.profiles p where p.id = auth.uid() and not p.is_founder)
  );

drop policy if exists staff_private_notes_delete on public.staff_private_notes;
create policy staff_private_notes_delete on public.staff_private_notes
  for delete to authenticated
  using (public.staff_private_note_can_delete(id));

comment on function public.staff_private_note_can_delete(uuid) is
  'Founder may delete all notes; Owner may delete own notes and Manager-authored notes; Managers may delete only their own notes.';
