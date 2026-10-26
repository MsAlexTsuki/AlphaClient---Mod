package com.alpharip.staffmod.staffops.ideas;

/** Evaluates simple rule comparisons against supplied values. */
public class DetectionRuleTester {
    public boolean atLeast(double value, double threshold) {
        return finite(value) && finite(threshold) && value >= threshold;
    }
    public boolean atMost(double value, double threshold) {
        return finite(value) && finite(threshold) && value <= threshold;
    }
    private boolean finite(double value) { return !Double.isNaN(value) && !Double.isInfinite(value); }
}