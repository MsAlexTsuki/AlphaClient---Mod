package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local-only case catalogue for prototyping an investigation UI. */
public final class InvestigationCases {
    private final List<CaseEntry> cases = new ArrayList<CaseEntry>();
    private long nextId = 1L;

    public CaseEntry create(String title, String summary, long createdAtMillis) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Case title must not be blank");
        }
        CaseEntry entry = new CaseEntry(nextId++, title.trim(),
                summary == null ? "" : summary, createdAtMillis);
        cases.add(entry);
        return entry;
    }

    public List<CaseEntry> getCases() {
        return Collections.unmodifiableList(new ArrayList<CaseEntry>(cases));
    }

    public static final class CaseEntry {
        private final long id;
        private final String title;
        private final String summary;
        private final long createdAtMillis;

        private CaseEntry(long id, String title, String summary, long createdAtMillis) {
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.createdAtMillis = createdAtMillis;
        }

        public long getId() { return id; }
        public String getTitle() { return title; }
        public String getSummary() { return summary; }
        public long getCreatedAtMillis() { return createdAtMillis; }
    }

    // TODO(Supabase): define authoritative case IDs, ownership, and access policies before syncing.
}