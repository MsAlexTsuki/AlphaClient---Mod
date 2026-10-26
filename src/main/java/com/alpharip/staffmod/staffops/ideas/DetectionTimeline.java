package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Stores detection-related timeline entries and can return them chronologically. */
public class DetectionTimeline {
    public static final class Entry {
        public final long timestampMillis;
        public final String type;
        public final String details;

        public Entry(long timestampMillis, String type, String details) {
            this.timestampMillis = timestampMillis;
            this.type = type == null ? "" : type;
            this.details = details == null ? "" : details;
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();

    public void add(long timestampMillis, String type, String details) {
        entries.add(new Entry(timestampMillis, type, details));
    }

    public List<Entry> getEntriesChronologically() {
        List<Entry> copy = new ArrayList<Entry>(entries);
        Collections.sort(copy, new Comparator<Entry>() {
            public int compare(Entry a, Entry b) {
                return a.timestampMillis < b.timestampMillis ? -1
                        : (a.timestampMillis == b.timestampMillis ? 0 : 1);
            }
        });
        return Collections.unmodifiableList(copy);
    }

    public void clear() { entries.clear(); }
}
