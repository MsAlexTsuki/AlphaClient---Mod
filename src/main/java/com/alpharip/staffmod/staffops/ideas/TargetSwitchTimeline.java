package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Foundational data holder for TargetSwitchTimeline; analysis and rendering are future integrations. */
public class TargetSwitchTimeline {
    private final List<Double> values = new ArrayList<Double>();

    public void addValue(double value) {
        if (!Double.isNaN(value) && !Double.isInfinite(value)) { values.add(value); }
    }
    public int getCount() { return values.size(); }
    public List<Double> getValues() {
        return Collections.unmodifiableList(new ArrayList<Double>(values));
    }
    public double getMean() {
        if (values.isEmpty()) { return 0.0D; }
        double total = 0.0D;
        for (Double value : values) { total += value; }
        return total / values.size();
    }
    public void clear() { values.clear(); }
}
