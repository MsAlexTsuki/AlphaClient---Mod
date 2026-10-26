package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Client-side catalogue of command permission identifiers for future UI display.
 * This class does not send commands or enforce server permissions.
 *
 * TODO(Supabase): define how command identifiers map to the canonical permission
 * records. Actual command authorization must remain server-side.
 */
public final class StaffCommandPermissions {
    public static final String FEATURE_NAME = "Staff Command Permissions";
    private final Set<String> commandKeys = new LinkedHashSet<String>();

    public void registerKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Command key cannot be blank");
        }
        commandKeys.add(key.trim());
    }

    public Set<String> getCommandKeys() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(commandKeys));
    }
}
