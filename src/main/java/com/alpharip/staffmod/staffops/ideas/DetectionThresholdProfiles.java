package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Stores named detection thresholds. */
public class DetectionThresholdProfiles {
    private final Map<String, Double> thresholds = new HashMap<String, Double>();
    public boolean set(String name, double threshold) {
        if (name == null || name.trim().isEmpty() || !finite(threshold)) { return false; }
        thresholds.put(name.trim(), threshold);
        return true;
    }
    public Double get(String name) { return name == null ? null : thresholds.get(name.trim()); }
    public Map<String, Double> snapshot() {
        return Collections.unmodifiableMap(new HashMap<String, Double>(thresholds));
    }
    private boolean finite(double value) { return !Double.isNaN(value) && !Double.isInfinite(value); }
}