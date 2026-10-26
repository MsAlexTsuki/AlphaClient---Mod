package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stores combat anomaly counters for a future dashboard view. */
public class CombatAnomalyDashboard {
    private final Map<String, Long> counters = new HashMap<String, Long>();

    public void increment(String anomalyType) {
        String key = normalize(anomalyType);
        if (key != null) {
            Long current = counters.get(key);
            counters.put(key, current == null ? 1L : current + 1L);
        }
    }

    public void addCount(String anomalyType, long amount) {
        String key = normalize(anomalyType);
        if (key != null && amount >= 0L) {
            Long current = counters.get(key);
            counters.put(key, (current == null ? 0L : current) + amount);
        }
    }

    public long getCount(String anomalyType) {
        String key = normalize(anomalyType);
        Long count = key == null ? null : counters.get(key);
        return count == null ? 0L : count;
    }

    public Map<String, Long> snapshot() {
        return Collections.unmodifiableMap(new HashMap<String, Long>(counters));
    }

    public void clear() {
        counters.clear();
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
