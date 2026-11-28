package com.alpharip.staffmod.actingrankassignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory registry; it does not grant or enforce any rank permissions. */
public final class ActingRankRegistry {
    private final Map<String, ActingRankEntry> entries = new LinkedHashMap<String, ActingRankEntry>();
    private int maximumEntries;

    public ActingRankRegistry(int maximumEntries) {
        setMaximumEntries(maximumEntries);
    }

    public synchronized void setMaximumEntries(int maximumEntries) {
        if (maximumEntries < 1) throw new IllegalArgumentException("Maximum entries must be positive.");
        this.maximumEntries = maximumEntries;
        while (entries.size() > maximumEntries) {
            String first = entries.keySet().iterator().next();
            entries.remove(first);
        }
    }

    public synchronized boolean add(ActingRankEntry entry) {
        if (entry == null || entries.containsKey(entry.getId()) || entries.size() >= maximumEntries) {
            return false;
        }
        entries.put(entry.getId(), entry);
        return true;
    }

    public synchronized ActingRankEntry get(String id) {
        return entries.get(id);
    }

    public synchronized boolean replace(ActingRankEntry entry) {
        if (entry == null || !entries.containsKey(entry.getId())) return false;
        entries.put(entry.getId(), entry);
        return true;
    }

    public synchronized List<ActingRankEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<ActingRankEntry>(entries.values()));
    }

    public synchronized List<ActingRankEntry> findByStaffName(String staffName) {
        List<ActingRankEntry> result = new ArrayList<ActingRankEntry>();
        if (staffName == null) return Collections.unmodifiableList(result);
        for (ActingRankEntry entry : entries.values()) {
            if (entry.getStaffName().equalsIgnoreCase(staffName.trim())) result.add(entry);
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int countActive(long nowMillis) {
        int count = 0;
        for (ActingRankEntry entry : entries.values()) if (entry.isActiveAt(nowMillis)) count++;
        return count;
    }

    public synchronized void clear() {
        entries.clear();
    }
}
