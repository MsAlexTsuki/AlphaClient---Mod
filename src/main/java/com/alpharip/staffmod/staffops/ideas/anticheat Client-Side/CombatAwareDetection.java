package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stores combat-context attributes supplied by a caller. */
public class CombatAwareDetection {
    private final Map<String, String> context = new HashMap<String, String>();
    public void put(String key, String value) {
        if (key != null && !key.trim().isEmpty() && value != null) {
            context.put(key.trim(), value);
        }
    }
    public String get(String key) { return key == null ? null : context.get(key.trim()); }
    public Map<String, String> snapshot() {
        return Collections.unmodifiableMap(new HashMap<String, String>(context));
    }
    public void clear() { context.clear(); }
}