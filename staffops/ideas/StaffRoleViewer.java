package com.alpharip.staffmod.staffops.ideas;

/**
 * Client-side view model for displaying a role and its known labels.
 * Data supplied here is display data, not proof of server-authorized access.
 *
 * TODO(Supabase): source verified community-role data from the agreed profile
 * and rank fields when the account integration is implemented.
 */
public final class StaffRoleViewer {
    public static final String FEATURE_NAME = "Staff Role Viewer";
    private String roleName;

    public StaffRoleViewer(String roleName) {
        setRoleName(roleName);
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be blank");
        }
        this.roleName = roleName.trim();
    }
}
