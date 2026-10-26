package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple local filters for audit entries represented as text.
 * TODO(Supabase): define structured audit fields before implementing server-side
 * filtering. Current filtering is local and text-based only.
 */
public final class StaffAuditFilters {
    public static final String FEATURE_NAME = "Staff Audit Filters";

    public List<String> containing(List<String> entries, String text) {
        if (entries == null || text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String needle = text.trim().toLowerCase(java.util.Locale.ROOT);
        List<String> result = new ArrayList<String>();
        for (String entry : entries) {
            if (entry != null && entry.toLowerCase(java.util.Locale.ROOT).contains(needle)) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
