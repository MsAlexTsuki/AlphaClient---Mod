package com.alpharip.staffmod.staffops.ideas;

/** Evaluates a supplied score against a proposed threshold. */
public class DetectionThresholdSimulator {
    public boolean exceeds(double score, double threshold) {
        return !Double.isNaN(score) && !Double.isInfinite(score)
                && !Double.isNaN(threshold) && !Double.isInfinite(threshold)
                && score >= threshold;
    }
}