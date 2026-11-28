package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Combines externally supplied signal scores using a simple weighted mean. */
public class MultiSignalDetector {
    private final Map<String, Double> scores = new HashMap<String, Double>();
    private final Map<String, Double> weights = new HashMap<String, Double>();

    public boolean setSignal(String name, double score, double weight) {
        String key = normalize(name);
        if (key == null || !isFinite(score) || !isFinite(weight) || weight < 0.0D) {
            return false;
        }
        scores.put(key, score);
        weights.put(key, weight);
        return true;
    }

    public double getCombinedScore() {
        double weightedTotal = 0.0D;
        double totalWeight = 0.0D;
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            double weight = weights.get(entry.getKey());
            weightedTotal += entry.getValue() * weight;
            totalWeight += weight;
        }
        return totalWeight == 0.0D ? 0.0D : weightedTotal / totalWeight;
    }

    public Map<String, Double> getScores() {
        return Collections.unmodifiableMap(new HashMap<String, Double>(scores));
    }

    public void clear() {
        scores.clear();
        weights.clear();
    }

    private boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
