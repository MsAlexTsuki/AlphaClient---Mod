package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records user-supplied local change-history entries for an investigation. */
public final class InvestigationChangeHistory {
    public static final class Entry {
        private final long timestampMillis;
        private final String actor;
        private final String change;

        public Entry(long timestampMillis, String actor, String change) {
            this.timestampMillis = timestampMillis;
            this.actor = actor == null ? "" : actor.trim();
            this.change = change == null ? "" : change.trim();
        }
        public long getTimestampMillis() { return timestampMillis; }
        public String getActor() { return actor; }
        public String getChange() { return change; }
    }

    private final List<Entry> entries = new ArrayList<Entry>();
    public synchronized void add(Entry entry) { if (entry != null) entries.add(entry); }
    public synchronized List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries));
    }
    public synchronized void clear() { entries.clear(); }
}