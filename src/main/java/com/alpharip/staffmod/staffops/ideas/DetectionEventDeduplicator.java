package com.alpharip.staffmod.staffops.ideas;

import java.util.HashSet;
import java.util.Set;

/** Tracks event identifiers to reject duplicates. */
public class DetectionEventDeduplicator {
    private final Set<String> seen = new HashSet<String>();
    public boolean firstOccurrence(String eventId) {
        return eventId != null && !eventId.trim().isEmpty() && seen.add(eventId.trim());
    }
    public void clear() { seen.clear(); }
    public int size() { return seen.size(); }
}