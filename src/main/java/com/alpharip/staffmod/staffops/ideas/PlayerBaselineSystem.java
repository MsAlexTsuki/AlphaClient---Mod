package com.alpharip.staffmod.staffops.ideas;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Stores manually supplied baseline values by player identifier and metric. */
public class PlayerBaselineSystem {
    private final Map<String, Map<String, Double>> baselines = new LinkedHashMap<String, Map<String, Double>>();

    public void setBaseline(String playerId, String metric, double value) {
        if (playerId == null || playerId.trim().isEmpty() || metric == null || metric.trim().isEmpty()) return;
        Map<String, Double> playerValues = baselines.get(playerId);
        if (playerValues == null) {
            playerValues = new LinkedHashMap<String, Double>();
            baselines.put(playerId, playerValues);
        }
        playerValues.put(metric, Double.valueOf(value));
    }

    public Double getBaseline(String playerId, String metric) {
        Map<String, Double> playerValues = baselines.get(playerId);
        return playerValues == null ? null : playerValues.get(metric);
    }

    public Map<String, Map<String, Double>> getBaselines() {
        Map<String, Map<String, Double>> copy = new LinkedHashMap<String, Map<String, Double>>();
        for (Map.Entry<String, Map<String, Double>> entry : baselines.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableMap(new LinkedHashMap<String, Double>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    public void clear() { baselines.clear(); }
}
