package com.alpharip.staffmod.store.cosmetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bounded, in-memory cosmetic catalogue populated by trusted AlphaClient code. */
public final class CosmeticRegistry {
    private static final int DEFAULT_MAXIMUM_ENTRIES = 1000;
    private final int maximumEntries;
    private final Map<String, CosmeticEntry> entries = new LinkedHashMap<String, CosmeticEntry>();

    public CosmeticRegistry() { this(DEFAULT_MAXIMUM_ENTRIES); }

    public CosmeticRegistry(int maximumEntries) {
        if (maximumEntries < 1) throw new IllegalArgumentException("Maximum entries must be positive.");
        this.maximumEntries = maximumEntries;
    }

    public synchronized boolean register(CosmeticEntry entry) {
        if (entry == null || entries.containsKey(entry.getId()) || entries.size() >= maximumEntries)
            return false;
        entries.put(entry.getId(), entry);
        return true;
    }

    public synchronized CosmeticEntry get(String id) { return entries.get(id); }

    public synchronized List<CosmeticEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<CosmeticEntry>(entries.values()));
    }

    public synchronized boolean remove(String id) { return entries.remove(id) != null; }
    public synchronized int size() { return entries.size(); }
    public synchronized void clear() { entries.clear(); }
}
