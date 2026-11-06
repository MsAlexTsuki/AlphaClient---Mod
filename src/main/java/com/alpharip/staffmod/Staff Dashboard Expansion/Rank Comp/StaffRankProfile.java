package com.alpharip.staffdashboardexpansion.rankcomp;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
/** A locally supplied role label and its manually entered permission labels. */
public final class StaffRankProfile {
    private final String roleName;
    private final Set<String> permissions;
    public StaffRankProfile(String roleName, String permissionInput) {
        if (roleName == null || roleName.trim().isEmpty()) throw new IllegalArgumentException("Role name is required.");
        this.roleName = roleName.trim();
        Set<String> values = new LinkedHashSet<String>();
        if (permissionInput != null) for (String entry : permissionInput.split(",")) {
            String value = entry.trim();
            if (!value.isEmpty()) values.add(value);
        }
        this.permissions = Collections.unmodifiableSet(values);
    }
    public String getRoleName() { return roleName; }
    public Set<String> getPermissions() { return permissions; }
}
