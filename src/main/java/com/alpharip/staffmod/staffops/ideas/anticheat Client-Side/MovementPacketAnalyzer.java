package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied movement-packet position samples. */
public class MovementPacketAnalyzer {
    public static final class PositionSample {
        public final long timestampMillis;
        public final double x;
        public final double y;
        public final double z;

        public PositionSample(long timestampMillis, double x, double y, double z) {
            this.timestampMillis = timestampMillis;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private final List<PositionSample> samples = new ArrayList<PositionSample>();

    public void recordPosition(long timestampMillis, double x, double y, double z) {
        samples.add(new PositionSample(timestampMillis, x, y, z));
    }

    public List<PositionSample> getSamples() {
        return Collections.unmodifiableList(new ArrayList<PositionSample>(samples));
    }

    public void clear() { samples.clear(); }
}
