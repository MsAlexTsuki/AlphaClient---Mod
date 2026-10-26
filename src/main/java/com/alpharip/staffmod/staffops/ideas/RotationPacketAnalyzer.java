package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied yaw and pitch values from rotation packet data. */
public class RotationPacketAnalyzer {
    public static final class RotationSample {
        public final long timestampMillis;
        public final float yaw;
        public final float pitch;

        public RotationSample(long timestampMillis, float yaw, float pitch) {
            this.timestampMillis = timestampMillis;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private final List<RotationSample> samples = new ArrayList<RotationSample>();

    public void recordRotation(long timestampMillis, float yaw, float pitch) {
        samples.add(new RotationSample(timestampMillis, yaw, pitch));
    }

    public List<RotationSample> getSamples() {
        return Collections.unmodifiableList(new ArrayList<RotationSample>(samples));
    }

    public void clear() { samples.clear(); }
}
