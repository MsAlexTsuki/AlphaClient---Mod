package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores fall and landing observations for review; it does not judge or punish players. */
public final class NoFallAnalyzer {
    public static final class FallObservation {
        private final long timestampMillis;
        private final double fallDistance;
        private final boolean landed;
        private final boolean damageReported;

        public FallObservation(long timestampMillis, double fallDistance,
                               boolean landed, boolean damageReported) {
            this.timestampMillis = timestampMillis;
            this.fallDistance = fallDistance;
            this.landed = landed;
            this.damageReported = damageReported;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public double getFallDistance() { return fallDistance; }
        public boolean isLanded() { return landed; }
        public boolean isDamageReported() { return damageReported; }
    }

    private final List<FallObservation> observations = new ArrayList<FallObservation>();
    public synchronized void add(FallObservation item) { if (item != null) observations.add(item); }
    public synchronized List<FallObservation> getObservations() {
        return Collections.unmodifiableList(new ArrayList<FallObservation>(observations));
    }
    public synchronized void clear() { observations.clear(); }
}