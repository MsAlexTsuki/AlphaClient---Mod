package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Calculates distances from caller-supplied coordinates; no attack packets are inspected. */
public final class ReachAnalyzer {
    public static final class Measurement {
        private final long timestampMillis;
        private final double distance;

        public Measurement(long timestampMillis, double distance) {
            this.timestampMillis = timestampMillis;
            this.distance = distance;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public double getDistance() { return distance; }
    }

    public double distance(double x1, double y1, double z1,
                           double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private final List<Measurement> measurements = new ArrayList<Measurement>();
    public synchronized void add(Measurement item) { if (item != null) measurements.add(item); }
    public synchronized List<Measurement> getMeasurements() {
        return Collections.unmodifiableList(new ArrayList<Measurement>(measurements));
    }
}