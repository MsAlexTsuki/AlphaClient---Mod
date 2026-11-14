package com.alpharip.staffmod.store.partners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bounded in-memory partner directory populated by trusted local AlphaClient code. */
public final class PartnerRegistry {
    private static final int DEFAULT_MAXIMUM_ENTRIES = 500;
    private final int maximumEntries;
    private final Map<String, PartnerEntry> entries = new LinkedHashMap<String, PartnerEntry>();

    public PartnerRegistry() { this(DEFAULT_MAXIMUM_ENTRIES); }

    public PartnerRegistry(int maximumEntries) {
        if (maximumEntries < 1) throw new IllegalArgumentException("Maximum entries must be positive.");
        this.maximumEntries = maximumEntries;
    }

    public synchronized boolean register(PartnerEntry entry) {
        if (entry == null || entries.containsKey(entry.getId()) || entries.size() >= maximumEntries)
            return false;
        entries.put(entry.getId(), entry);
        return true;
    }

    public synchronized PartnerEntry get(String id) { return entries.get(id); }

    public synchronized List<PartnerEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<PartnerEntry>(entries.values()));
    }

    public synchronized boolean remove(String id) { return entries.remove(id) != null; }
    public synchronized int size() { return entries.size(); }
    public synchronized void clear() { entries.clear(); }
}
