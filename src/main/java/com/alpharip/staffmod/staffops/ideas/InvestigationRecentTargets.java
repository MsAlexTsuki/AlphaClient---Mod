package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Tracks recently opened targets in memory, newest first. */
public final class InvestigationRecentTargets {
    private final Map<String, Long> openedAt = new LinkedHashMap<String, Long>();

    public synchronized void record(String targetId, long timestampMillis) {
        if (targetId == null || targetId.trim().isEmpty()) return;
        openedAt.remove(targetId);
        openedAt.put(targetId, Long.valueOf(timestampMillis));
    }

    public synchronized Long getOpenedAt(String targetId) { return openedAt.get(targetId); }

    public synchronized java.util.List<String> getNewestFirst() {
        java.util.List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(openedAt.entrySet());
        Collections.sort(entries, new java.util.Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> a, Map.Entry<String, Long> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });
        java.util.List<String> result = new ArrayList<String>();
        for (Map.Entry<String, Long> entry : entries) result.add(entry.getKey());
        return Collections.unmodifiableList(result);
    }
}