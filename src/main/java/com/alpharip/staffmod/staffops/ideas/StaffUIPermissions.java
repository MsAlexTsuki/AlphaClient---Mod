package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Client-side catalogue of UI visibility permission identifiers.
 * Hiding a UI element is not a security control and does not grant access.
 *
 * TODO(Supabase): connect these identifiers to the canonical permission mapping
 * if account-backed visibility rules are required.
 */
public final class StaffUIPermissions {
    public static final String FEATURE_NAME = "Staff UI Permissions";
    private final Set<String> uiKeys = new LinkedHashSet<String>();

    public void registerKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("UI permission key cannot be blank");
        }
        uiKeys.add(key.trim());
    }

    public Set<String> getUIKeys() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(uiKeys));
    }
}
