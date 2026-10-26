package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tracks caller-supplied observations for each player and behavior metric. */
public class BehaviorBaselineTracker {
    private final Map<String, Map<String, List<Double>>> observations =
            new LinkedHashMap<String, Map<String, List<Double>>>();

    public void record(String playerId, String metric, double value) {
        if (playerId == null || playerId.trim().isEmpty() || metric == null || metric.trim().isEmpty()
                || Double.isNaN(value) || Double.isInfinite(value)) return;
        Map<String, List<Double>> metrics = observations.get(playerId);
        if (metrics == null) {
            metrics = new LinkedHashMap<String, List<Double>>();
            observations.put(playerId, metrics);
        }
        List<Double> values = metrics.get(metric);
        if (values == null) {
            values = new ArrayList<Double>();
            metrics.put(metric, values);
        }
        values.add(Double.valueOf(value));
    }

    public List<Double> getObservations(String playerId, String metric) {
        Map<String, List<Double>> metrics = observations.get(playerId);
        List<Double> values = metrics == null ? null : metrics.get(metric);
        return values == null ? Collections.<Double>emptyList()
                : Collections.unmodifiableList(new ArrayList<Double>(values));
    }

    public void clear() { observations.clear(); }
}
