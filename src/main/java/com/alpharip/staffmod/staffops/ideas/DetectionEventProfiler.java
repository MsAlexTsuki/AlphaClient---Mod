package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Counts caller-supplied event categories. */
public class DetectionEventProfiler {
    private final Map<String, Long> counts = new HashMap<String, Long>();
    public void record(String name, long amount) {
        if (name == null || name.trim().isEmpty() || amount < 0L) { return; }
        String key = name.trim();
        Long current = counts.get(key);
        counts.put(key, (current == null ? 0L : current) + amount);
    }
    public long get(String name) {
        Long value = name == null ? null : counts.get(name.trim());
        return value == null ? 0L : value;
    }
    public Map<String, Long> snapshot() {
        return Collections.unmodifiableMap(new HashMap<String, Long>(counts));
    }
    public void clear() { counts.clear(); }
}