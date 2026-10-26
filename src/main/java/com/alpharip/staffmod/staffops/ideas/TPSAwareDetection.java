package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores server tick-rate measurements for context-aware evaluation. */
public class TPSAwareDetection {
    private final List<Double> samples = new ArrayList<Double>();
    public void record(double value) {
        if (!Double.isNaN(value) && !Double.isInfinite(value) && value >= 0.0D) {
            samples.add(value);
        }
    }
    public List<Double> snapshot() {
        return Collections.unmodifiableList(new ArrayList<Double>(samples));
    }
    public double average() {
        if (samples.isEmpty()) { return 0.0D; }
        double sum = 0.0D;
        for (Double value : samples) { sum += value; }
        return sum / samples.size();
    }
    public void clear() { samples.clear(); }
}