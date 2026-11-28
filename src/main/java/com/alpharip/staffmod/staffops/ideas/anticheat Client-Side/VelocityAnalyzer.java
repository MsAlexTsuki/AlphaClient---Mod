package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied velocity observations for review; does not inspect live game events. */
public final class VelocityAnalyzer {
    public static final class Observation {
        private final long timestampMillis;
        private final double x;
        private final double y;
        private final double z;

        public Observation(long timestampMillis, double x, double y, double z) {
            this.timestampMillis = timestampMillis;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public double magnitude() { return Math.sqrt(x * x + y * y + z * z); }
    }

    private final List<Observation> observations = new ArrayList<Observation>();
    public synchronized void add(Observation value) { if (value != null) observations.add(value); }
    public synchronized List<Observation> getObservations() {
        return Collections.unmodifiableList(new ArrayList<Observation>(observations));
    }
    public synchronized void clear() { observations.clear(); }
}