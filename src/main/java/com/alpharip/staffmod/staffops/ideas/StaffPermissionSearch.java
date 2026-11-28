package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Searches a local list of permission identifiers for UI filtering.
 *
 * TODO(Supabase): populate the search index from the canonical permission
 * catalogue once that schema exists. This class must remain read-only.
 */
public final class StaffPermissionSearch {
    public static final String FEATURE_NAME = "Staff Permission Search";

    public List<String> search(List<String> permissions, String query) {
        if (permissions == null || query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<String>();
        for (String permission : permissions) {
            if (permission != null
                    && permission.toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(permission);
            }
        }
        return Collections.unmodifiableList(matches);
    }
}
