package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Client-side catalogue of tool permission keys for later UI integration.
 * Listing a key here does not enable the tool or authorize its use.
 *
 * TODO(Supabase): map keys to the authoritative permission records and define
 * read-only client access before connecting this catalogue to live accounts.
 */
public final class StaffToolPermissions {
    public static final String FEATURE_NAME = "Staff Tool Permissions";
    private final Set<String> permissionKeys = new LinkedHashSet<String>();

    public void registerKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission key cannot be blank");
        }
        permissionKeys.add(key.trim());
    }

    public Set<String> getPermissionKeys() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(permissionKeys));
    }
}
