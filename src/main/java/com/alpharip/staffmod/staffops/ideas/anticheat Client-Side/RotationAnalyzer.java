package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores caller-provided rotation deltas; no automated cheating classification is performed. */
public final class RotationAnalyzer {
    public static final class Delta {
        private final long timestampMillis;
        private final float yawDelta;
        private final float pitchDelta;

        public Delta(long timestampMillis, float yawDelta, float pitchDelta) {
            this.timestampMillis = timestampMillis;
            this.yawDelta = yawDelta;
            this.pitchDelta = pitchDelta;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public float getYawDelta() { return yawDelta; }
        public float getPitchDelta() { return pitchDelta; }
    }

    private final List<Delta> deltas = new ArrayList<Delta>();
    public synchronized void add(Delta delta) { if (delta != null) deltas.add(delta); }
    public synchronized List<Delta> getDeltas() {
        return Collections.unmodifiableList(new ArrayList<Delta>(deltas));
    }
    public synchronized void clear() { deltas.clear(); }
}