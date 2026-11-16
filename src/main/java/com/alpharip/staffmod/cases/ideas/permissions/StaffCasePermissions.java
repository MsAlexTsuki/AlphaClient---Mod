package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Local catalogue for case-related permission keys; no shared case system is used.
 * TODO(Supabase): define case permission records and row-level access rules before
 * connecting this catalogue to shared case data.
 */
public final class StaffCasePermissions {
    public static final String FEATURE_NAME = "Staff Case Permissions";
    private final Set<String> keys = new LinkedHashSet<String>();

    public void register(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Case permission key cannot be blank");
        }
        keys.add(key.trim());
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(keys));
    }
}
