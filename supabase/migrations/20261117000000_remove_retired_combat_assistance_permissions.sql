-- Remove the four retired Founder-only combat assistance modules and their permissions.
-- Keep historical audit rows; they retain their original module identifiers as text.

ALTER TABLE public.client_mod_permission_audit
    DROP CONSTRAINT IF EXISTS client_mod_permission_audit_mod_id_fkey;

DELETE FROM public.client_mods
WHERE mod_id IN (
    'aim-assist',
    'w-tap-assistance',
    's-tap-assistance',
    'auto-strafe'
);

-- Dependent rank permissions, per-user overrides, aliases, notes, and command
-- rank permissions are removed through their existing ON DELETE CASCADE rules.
DELETE FROM public.client_command_permission_catalog
WHERE permission_key IN (
    'command.aimassist',
    'command.stap',
    'command.wtap',
    'mod.w-tap-assistance',
    'menu.aimasisst',
    'menu.stap'
);

COMMENT ON COLUMN public.client_mod_permission_audit.mod_id IS
    'Historical module identifier; retired module IDs remain in audit history after catalog deletion.';
