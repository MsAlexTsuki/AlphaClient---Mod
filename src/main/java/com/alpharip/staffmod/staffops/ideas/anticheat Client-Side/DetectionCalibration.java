package com.alpharip.staffmod.staffops.ideas;

/** Tracks calibration samples and a simple running mean. */
public class DetectionCalibration {
    private long sampleCount;
    private double sampleSum;

    public void addSample(double sample) {
        if (Double.isNaN(sample) || Double.isInfinite(sample)) {
            throw new IllegalArgumentException("sample must be finite");
        }
        sampleSum += sample;
        sampleCount++;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public double getMean() {
        return sampleCount == 0L ? 0.0D : sampleSum / sampleCount;
    }

    public boolean isCalibrated(long minimumSamples) {
        return sampleCount >= Math.max(1L, minimumSamples);
    }

    public void reset() {
        sampleCount = 0L;
        sampleSum = 0.0D;
    }
}
