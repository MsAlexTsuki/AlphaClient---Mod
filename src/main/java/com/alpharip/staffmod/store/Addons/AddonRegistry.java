package com.alpharip.staffmod.store.addons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bounded in-memory catalogue. No remote catalogue or persistence is used. */
public final class AddonRegistry {
    private final Map<String, AddonEntry> entries = new LinkedHashMap<String, AddonEntry>();
    private final Map<String, AddonInstallState> states = new LinkedHashMap<String, AddonInstallState>();
    private final int maximumEntries;

    public AddonRegistry() { this(500); }
    public AddonRegistry(int maximumEntries) {
        if (maximumEntries < 1) throw new IllegalArgumentException("Maximum entries must be positive.");
        this.maximumEntries = maximumEntries;
    }

    public synchronized boolean register(AddonEntry entry) {
        if (entry == null || entries.containsKey(entry.getId()) || entries.size() >= maximumEntries) return false;
        entries.put(entry.getId(), entry);
        states.put(entry.getId(), AddonInstallState.AVAILABLE);
        return true;
    }

    public synchronized AddonEntry get(String id) { return entries.get(id); }
    public synchronized AddonInstallState getState(String id) { return states.get(id); }

    synchronized boolean setState(String id, AddonInstallState state) {
        if (!entries.containsKey(id) || state == null) return false;
        states.put(id, state);
        return true;
    }

    public synchronized List<AddonEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<AddonEntry>(entries.values()));
    }

    public synchronized Collection<String> getIds() {
        return Collections.unmodifiableList(new ArrayList<String>(entries.keySet()));
    }

    public synchronized boolean remove(String id) {
        states.remove(id);
        return entries.remove(id) != null;
    }

    public synchronized void clear() { entries.clear(); states.clear(); }
    public synchronized int size() { return entries.size(); }
}
