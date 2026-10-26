package com.alpharip.staffmod.staffops.ideas;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

/** Local key/value labels for evidence metadata. */
public final class EvidenceLabels {
    private final Map<String, String> labels = new LinkedHashMap<String, String>();

    public void put(String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Label key must not be blank");
        }
        labels.put(key.trim(), value == null ? "" : value.trim());
    }

    public String get(String key) { return labels.get(key); }

    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(labels));
    }
}