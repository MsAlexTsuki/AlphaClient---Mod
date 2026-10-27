package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Foundational data model for GroundMovementAnalyzer; callers provide observations explicitly. */
public class GroundMovementAnalyzer {
    private final List<Double> observations = new ArrayList<Double>();

    public void addObservation(double value) {
        if (!Double.isNaN(value) && !Double.isInfinite(value)) {
            observations.add(value);
        }
    }

    public List<Double> getObservations() {
        return Collections.unmodifiableList(new ArrayList<Double>(observations));
    }

    public int getObservationCount() {
        return observations.size();
    }

    public double getAverage() {
        if (observations.isEmpty()) { return 0.0D; }
        double total = 0.0D;
        for (Double value : observations) { total += value; }
        return total / observations.size();
    }

    public void clear() {
        observations.clear();
    }
}
