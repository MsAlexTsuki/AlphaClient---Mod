package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores named benchmark measurements. */
public class DetectionBenchmarkSuite {
    private final List<Double> samples = new ArrayList<Double>();
    public void record(double value) {
        if (!Double.isNaN(value) && !Double.isInfinite(value)) { samples.add(value); }
    }
    public List<Double> snapshot() {
        return Collections.unmodifiableList(new ArrayList<Double>(samples));
    }
    public int size() { return samples.size(); }
    public double average() {
        if (samples.isEmpty()) { return 0.0D; }
        double total = 0.0D;
        for (Double value : samples) { total += value; }
        return total / samples.size();
    }
    public void clear() { samples.clear(); }
}