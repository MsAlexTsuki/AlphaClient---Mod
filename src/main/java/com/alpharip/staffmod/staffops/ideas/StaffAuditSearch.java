package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Searches a supplied local list of audit text.
 * TODO(Supabase): query a read-only audit source only after its schema and access
 * policy are defined. This class currently performs no network requests.
 */
public final class StaffAuditSearch {
    public static final String FEATURE_NAME = "Staff Audit Search";

    public List<String> search(List<String> entries, String query) {
        if (entries == null || query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<String>();
        for (String entry : entries) {
            if (entry != null && entry.toLowerCase(Locale.ROOT).contains(needle)) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
