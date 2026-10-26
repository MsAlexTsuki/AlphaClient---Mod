package com.alpharip.staffmod.staffops.ideas;

/** Counts reviewed detections and confirmed false positives. */
public class FalsePositiveTracker {
    private long reviewedCount;
    private long falsePositiveCount;

    public void recordReview(boolean falsePositive) {
        reviewedCount++;
        if (falsePositive) {
            falsePositiveCount++;
        }
    }

    public long getReviewedCount() {
        return reviewedCount;
    }

    public long getFalsePositiveCount() {
        return falsePositiveCount;
    }

    public double getFalsePositiveRate() {
        return reviewedCount == 0L ? 0.0D
                : (double) falsePositiveCount / (double) reviewedCount;
    }

    public void reset() {
        reviewedCount = 0L;
        falsePositiveCount = 0L;
    }
}
