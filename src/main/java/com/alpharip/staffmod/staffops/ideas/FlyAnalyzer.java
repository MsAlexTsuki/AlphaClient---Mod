package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records supplied flight-related observations for review; it does not detect or flag players. */
public final class FlyAnalyzer {
    public static final class Observation {
        private final long timestampMillis;
        private final double verticalDelta;
        private final boolean onGround;

        public Observation(long timestampMillis, double verticalDelta, boolean onGround) {
            this.timestampMillis = timestampMillis;
            this.verticalDelta = verticalDelta;
            this.onGround = onGround;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public double getVerticalDelta() { return verticalDelta; }
        public boolean isOnGround() { return onGround; }
    }

    private final List<Observation> observations = new ArrayList<Observation>();
    public synchronized void add(Observation observation) {
        if (observation != null) observations.add(observation);
    }
    public synchronized List<Observation> getObservations() {
        return Collections.unmodifiableList(new ArrayList<Observation>(observations));
    }
    public synchronized void clear() { observations.clear(); }
}