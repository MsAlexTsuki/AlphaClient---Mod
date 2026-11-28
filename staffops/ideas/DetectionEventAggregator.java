package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Aggregates counts by event category. */
public class DetectionEventAggregator {
    private final Map<String, Integer> values = new HashMap<String, Integer>();
    public void record(String key) {
        if (key == null || key.trim().isEmpty()) { return; }
        String normalized = key.trim();
        Integer current = values.get(normalized);
        values.put(normalized, current == null ? 1 : current + 1);
    }
    public int get(String key) {
        Integer value = key == null ? null : values.get(key.trim());
        return value == null ? 0 : value;
    }
    public Map<String, Integer> snapshot() {
        return Collections.unmodifiableMap(new HashMap<String, Integer>(values));
    }
    public void clear() { values.clear(); }
}