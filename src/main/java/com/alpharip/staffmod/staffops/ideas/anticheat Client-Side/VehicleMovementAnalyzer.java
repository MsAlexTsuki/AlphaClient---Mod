package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Foundational observation store for VehicleMovementAnalyzer; no automatic game-event collection is included. */
public class VehicleMovementAnalyzer {
    private final List<Double> values = new ArrayList<Double>();

    public void record(double value) {
        if (!Double.isNaN(value) && !Double.isInfinite(value)) {
            values.add(value);
        }
    }

    public int size() { return values.size(); }

    public List<Double> snapshot() {
        return Collections.unmodifiableList(new ArrayList<Double>(values));
    }

    public double mean() {
        if (values.isEmpty()) { return 0.0D; }
        double sum = 0.0D;
        for (Double value : values) { sum += value; }
        return sum / values.size();
    }

    public void reset() { values.clear(); }
}
