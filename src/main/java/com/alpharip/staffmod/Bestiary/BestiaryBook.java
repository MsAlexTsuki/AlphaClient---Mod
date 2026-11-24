package com.alpharip.staffmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory collection of discovered mob types. */
public final class BestiaryBook {
    private final Map<String, BestiaryEntry> entries = new LinkedHashMap<String, BestiaryEntry>();

    public synchronized BestiaryEntry record(String entityId, String displayName, String world, long timestamp) {
        BestiaryEntry entry = entries.get(entityId);
        if (entry == null) {
            entry = new BestiaryEntry(entityId, displayName, world, timestamp);
            entries.put(entityId, entry);
        } else {
            entry.encounteredAgain(timestamp);
        }
        return entry;
    }

    public synchronized List<BestiaryEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<BestiaryEntry>(entries.values()));
    }

    public synchronized int getDiscoveredMobCount() { return entries.size(); }
    public synchronized void clear() { entries.clear(); }
}
