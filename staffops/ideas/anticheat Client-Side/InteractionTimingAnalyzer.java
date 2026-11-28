package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Foundational timing/sequence data holder for InteractionTimingAnalyzer; no live event hook is included. */
public class InteractionTimingAnalyzer {
    private final List<Long> samples = new ArrayList<Long>();

    public void record(long sample) {
        if (sample >= 0L) { samples.add(sample); }
    }
    public int getCount() { return samples.size(); }
    public List<Long> getSamples() {
        return Collections.unmodifiableList(new ArrayList<Long>(samples));
    }
    public double getAverage() {
        if (samples.isEmpty()) { return 0.0D; }
        double sum = 0.0D;
        for (Long sample : samples) { sum += sample; }
        return sum / samples.size();
    }
    public void clear() { samples.clear(); }
}
