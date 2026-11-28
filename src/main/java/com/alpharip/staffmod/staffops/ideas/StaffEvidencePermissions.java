package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Local catalogue of evidence permission identifiers only.
 * TODO(Supabase): define authoritative evidence permission records and read policy.
 * Evidence storage and access enforcement are not implemented here.
 */
public final class StaffEvidencePermissions {
    public static final String FEATURE_NAME = "Staff Evidence Permissions";
    private final Set<String> keys = new LinkedHashSet<String>();

    public void register(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Evidence permission key cannot be blank");
        }
        keys.add(key.trim());
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(keys));
    }
}
