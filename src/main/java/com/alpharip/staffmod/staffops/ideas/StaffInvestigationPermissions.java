package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Local catalogue of investigation permission keys; it does not authorize actions.
 * TODO(Supabase): map keys to the authoritative permission schema when connected.
 */
public final class StaffInvestigationPermissions {
    public static final String FEATURE_NAME = "Staff Investigation Permissions";
    private final Set<String> keys = new LinkedHashSet<String>();

    public void register(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Investigation permission key cannot be blank");
        }
        keys.add(key.trim());
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(keys));
    }
}
