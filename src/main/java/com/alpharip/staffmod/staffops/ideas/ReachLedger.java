package com.alpharip.staffmod.staffops.ideas;
import java.util.Map;
import java.util.TreeMap;
/** Per-session hit-distance histogram; hit event collection and chart rendering are pending. */
public final class ReachLedger {
    private final double bucketSize;
    private final Map<Double, Integer> histogram = new TreeMap<Double, Integer>();
    private int totalHits;
    public ReachLedger() { this(0.25D); }
    public ReachLedger(double bucketSize) {
        if (bucketSize <= 0D) throw new IllegalArgumentException("bucketSize");
        this.bucketSize = bucketSize;
    }
    public void recordHit(double distance) {
        if (Double.isNaN(distance) || Double.isInfinite(distance) || distance < 0D) return;
        double bucket = Math.floor(distance / bucketSize) * bucketSize;
        Double key = Double.valueOf(bucket);
        Integer count = histogram.get(key);
        histogram.put(key, Integer.valueOf(count == null ? 1 : count.intValue() + 1));
        totalHits++;
    }
    public Map<Double, Integer> getHistogram() { return new TreeMap<Double, Integer>(histogram); }
    public int getTotalHits() { return totalHits; }
    public void resetSession() { histogram.clear(); totalHits = 0; }
}
