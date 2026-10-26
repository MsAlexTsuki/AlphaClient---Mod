package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Maps alert levels to destination labels. */
public class DetectionAlertRouting {
    private final Map<String, String> routes = new HashMap<String, String>();
    public boolean setRoute(String level, String destination) {
        if (blank(level) || blank(destination)) { return false; }
        routes.put(level.trim(), destination.trim());
        return true;
    }
    public String getRoute(String level) { return level == null ? null : routes.get(level.trim()); }
    public Map<String, String> snapshot() {
        return Collections.unmodifiableMap(new HashMap<String, String>(routes));
    }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}