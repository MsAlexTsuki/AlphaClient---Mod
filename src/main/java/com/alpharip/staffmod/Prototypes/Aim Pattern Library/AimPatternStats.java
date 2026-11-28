package com.alpharip.staffmod.aimpatternlibrary;

/** Immutable descriptive statistics for a pattern's numeric observations. */
public final class AimPatternStats {
    private final int count;
    private final double mean;
    private final double minimum;
    private final double maximum;
    private final double standardDeviation;

    public AimPatternStats(int count, double mean, double minimum, double maximum, double standardDeviation) {
        this.count = count;
        this.mean = mean;
        this.minimum = minimum;
        this.maximum = maximum;
        this.standardDeviation = standardDeviation;
    }
    public int getCount() { return count; }
    public double getMean() { return mean; }
    public double getMinimum() { return minimum; }
    public double getMaximum() { return maximum; }
    public double getStandardDeviation() { return standardDeviation; }

    public static AimPatternStats calculate(AimPattern pattern) {
        if (pattern == null || pattern.getObservations().isEmpty()) {
            return new AimPatternStats(0, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        int count = pattern.getObservationCount();
        double sum = 0.0D, min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (Double value : pattern.getObservations()) {
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double mean = sum / count, squared = 0.0D;
        for (Double value : pattern.getObservations()) {
            double delta = value - mean;
            squared += delta * delta;
        }
        return new AimPatternStats(count, mean, min, max, Math.sqrt(squared / count));
    }
}
