package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied aim-angle samples for later review; it does not inspect live rotations. */
public final class AimAnalyzer {
    public static final class Sample {
        private final long timestampMillis;
        private final float yaw;
        private final float pitch;

        public Sample(long timestampMillis, float yaw, float pitch) {
            this.timestampMillis = timestampMillis;
            this.yaw = yaw;
            this.pitch = pitch;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public float getYaw() { return yaw; }
        public float getPitch() { return pitch; }
    }

    private final List<Sample> samples = new ArrayList<Sample>();
    public synchronized void add(Sample sample) { if (sample != null) samples.add(sample); }
    public synchronized List<Sample> getSamples() {
        return Collections.unmodifiableList(new ArrayList<Sample>(samples));
    }
    public synchronized void clear() { samples.clear(); }
}