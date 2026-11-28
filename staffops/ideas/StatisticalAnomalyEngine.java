package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Provides descriptive statistics for supplied numeric observations. */
public class StatisticalAnomalyEngine {
    private final List<Double> observations = new ArrayList<Double>();

    public void record(double value) {
        if (!Double.isNaN(value) && !Double.isInfinite(value)) observations.add(Double.valueOf(value));
    }

    public List<Double> getObservations() {
        return Collections.unmodifiableList(new ArrayList<Double>(observations));
    }

    public double getMean() {
        if (observations.isEmpty()) return 0.0D;
        double total = 0.0D;
        for (Double value : observations) total += value.doubleValue();
        return total / observations.size();
    }

    public double getPopulationVariance() {
        if (observations.isEmpty()) return 0.0D;
        double mean = getMean();
        double total = 0.0D;
        for (Double value : observations) {
            double difference = value.doubleValue() - mean;
            total += difference * difference;
        }
        return total / observations.size();
    }

    public void clear() { observations.clear(); }
}
