package com.alpharip.staffmod.timing.blockbreak;

/** Immutable interval between two consecutive block-break events. */
public final class BlockBreakSample {
    private final long intervalMillis;
    private final long recordedAtMillis;

    public BlockBreakSample(long intervalMillis, long recordedAtMillis) {
        if (intervalMillis < 0L) throw new IllegalArgumentException("intervalMillis cannot be negative");
        this.intervalMillis = intervalMillis;
        this.recordedAtMillis = recordedAtMillis;
    }

    public long getIntervalMillis() { return intervalMillis; }
    /** Returns -1 when the sample was supplied through the compatibility record(long) method. */
    public long getRecordedAtMillis() { return recordedAtMillis; }
}
