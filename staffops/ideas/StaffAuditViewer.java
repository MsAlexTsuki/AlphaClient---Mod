package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local, in-memory audit entry viewer foundation.
 * This does not read server logs or create authoritative audit records.
 * TODO(Supabase): specify an append-only audit schema and read permissions if
 * shared audit records are later required.
 */
public final class StaffAuditViewer {
    public static final String FEATURE_NAME = "Staff Audit Viewer";
    private final List<String> entries = new ArrayList<String>();

    public void addLocalEntry(String entry) {
        if (entry == null || entry.trim().isEmpty()) {
            throw new IllegalArgumentException("Audit entry cannot be blank");
        }
        entries.add(entry.trim());
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(new ArrayList<String>(entries));
    }

    public void clearLocalEntries() {
        entries.clear();
    }
}
