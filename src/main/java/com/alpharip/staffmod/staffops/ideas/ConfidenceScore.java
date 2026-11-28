package com.alpharip.staffmod.staffops.ideas;

/** Holds a confidence value supplied by a caller, normalized to the 0-1 range. */
public class ConfidenceScore {
    private double value;

    public double getValue() { return value; }

    public void setValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return;
        this.value = Math.max(0.0D, Math.min(1.0D, value));
    }

    public int getPercentage() { return (int) Math.round(value * 100.0D); }
}
