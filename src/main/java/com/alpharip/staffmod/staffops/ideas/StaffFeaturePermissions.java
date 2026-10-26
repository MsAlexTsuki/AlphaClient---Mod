package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Client-side catalogue of feature permission keys.
 * This catalogue is descriptive only and must not be used as an authorization gate.
 *
 * TODO(Supabase): connect feature keys to the canonical permission mapping when
 * implementing account-backed permission display.
 */
public final class StaffFeaturePermissions {
    public static final String FEATURE_NAME = "Staff Feature Permissions";
    private final Set<String> featureKeys = new LinkedHashSet<String>();

    public void registerKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Feature key cannot be blank");
        }
        featureKeys.add(key.trim());
    }

    public Set<String> getFeatureKeys() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(featureKeys));
    }
}
