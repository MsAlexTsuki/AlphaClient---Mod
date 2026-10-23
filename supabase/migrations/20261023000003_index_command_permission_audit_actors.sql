create index if not exists idx_client_command_rank_permissions_updated_by
    on public.client_command_rank_permissions(updated_by);
create index if not exists idx_client_command_user_overrides_updated_by
    on public.client_command_user_overrides(updated_by);