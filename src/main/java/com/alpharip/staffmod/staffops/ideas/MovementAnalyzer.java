package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores movement samples for later local analysis; does not inspect live gameplay. */
public final class MovementAnalyzer {
    public static final class Sample {
        private final long timestampMillis;
        private final double x;
        private final double y;
        private final double z;

        public Sample(long timestampMillis, double x, double y, double z) {
            this.timestampMillis = timestampMillis;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
    }

    private final List<Sample> samples = new ArrayList<Sample>();
    public synchronized void addSample(Sample sample) { if (sample != null) samples.add(sample); }
    public synchronized List<Sample> getSamples() {
        return Collections.unmodifiableList(new ArrayList<Sample>(samples));
    }
    public synchronized void clear() { samples.clear(); }
}