package com.alpharip.staffmod.staffops.ideas;


/** Local time-window settings for a future evidence timeline view. */
public final class EvidenceTimelineZoom {
    private long startMillis;
    private long endMillis;

    public void setWindow(long startMillis, long endMillis) {
        if (endMillis < startMillis) {
            throw new IllegalArgumentException("endMillis must not precede startMillis");
        }
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }

    public long getStartMillis() { return startMillis; }
    public long getEndMillis() { return endMillis; }
    public long getDurationMillis() { return endMillis - startMillis; }

    public boolean contains(long timestampMillis) {
        return timestampMillis >= startMillis && timestampMillis <= endMillis;
    }
}