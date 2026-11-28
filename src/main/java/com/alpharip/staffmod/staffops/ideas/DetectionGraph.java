package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Stores simple directed relationships between caller-supplied detection nodes. */
public class DetectionGraph {
    private final Map<String, Set<String>> edges = new LinkedHashMap<String, Set<String>>();

    public void connect(String from, String to) {
        if (from == null || from.trim().isEmpty() || to == null || to.trim().isEmpty()) return;
        Set<String> targets = edges.get(from);
        if (targets == null) {
            targets = new LinkedHashSet<String>();
            edges.put(from, targets);
        }
        targets.add(to);
    }

    public Map<String, Set<String>> getEdges() {
        Map<String, Set<String>> copy = new LinkedHashMap<String, Set<String>>();
        for (Map.Entry<String, Set<String>> entry : edges.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableSet(new LinkedHashSet<String>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    public void clear() { edges.clear(); }
}
