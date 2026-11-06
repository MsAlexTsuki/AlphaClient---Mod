package com.alpharip.staffmod.timing.blockplacement;

/** Immutable measurement of one interval between block placements. */
public final class BlockPlacementSample {
    private final long intervalMillis;
    private final long recordedAtMillis;

    public BlockPlacementSample(long intervalMillis, long recordedAtMillis) {
        if (intervalMillis < 0L) {
            throw new IllegalArgumentException("intervalMillis cannot be negative");
        }
        this.intervalMillis = intervalMillis;
        this.recordedAtMillis = recordedAtMillis;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    /** Returns -1 when the sample was added through the legacy record API. */
    public long getRecordedAtMillis() {
        return recordedAtMillis;
    }
}
