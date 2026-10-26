package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores timestamped packet-burst records. */
public class PacketBurstTimeline {
    public static class Sample {
        private final long timestamp;
        private final double value;
        public Sample(long timestamp, double value) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException("value must be finite");
            }
            this.timestamp = timestamp;
            this.value = value;
        }
        public long getTimestamp() { return timestamp; }
        public double getValue() { return value; }
    }
    private final List<Sample> samples = new ArrayList<Sample>();
    public void add(long timestamp, double value) { samples.add(new Sample(timestamp, value)); }
    public List<Sample> snapshot() {
        return Collections.unmodifiableList(new ArrayList<Sample>(samples));
    }
    public void clear() { samples.clear(); }
}