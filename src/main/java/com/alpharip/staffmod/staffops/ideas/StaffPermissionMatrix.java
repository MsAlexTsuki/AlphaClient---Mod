package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local display model for a permission matrix. It does not grant permissions.
 *
 * TODO(Supabase): connect to the authoritative permission source only when
 * implementing the feature; define read-only access and the permission schema.
 */
public final class StaffPermissionMatrix {
    public static final String FEATURE_NAME = "Staff Permission Matrix";
    private final Map<String, Map<String, Boolean>> matrix =
            new LinkedHashMap<String, Map<String, Boolean>>();

    public void setPermission(String role, String permission, boolean allowed) {
        requireText(role, "role");
        requireText(permission, "permission");
        Map<String, Boolean> rolePermissions = matrix.get(role);
        if (rolePermissions == null) {
            rolePermissions = new LinkedHashMap<String, Boolean>();
            matrix.put(role, rolePermissions);
        }
        rolePermissions.put(permission, Boolean.valueOf(allowed));
    }

    public Boolean getPermission(String role, String permission) {
        Map<String, Boolean> rolePermissions = matrix.get(role);
        return rolePermissions == null ? null : rolePermissions.get(permission);
    }

    public List<String> getRoles() {
        return Collections.unmodifiableList(new ArrayList<String>(matrix.keySet()));
    }

    public Map<String, Boolean> getPermissionsForRole(String role) {
        Map<String, Boolean> permissions = matrix.get(role);
        if (permissions == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<String, Boolean>(permissions));
    }

    private static void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
    }
}
