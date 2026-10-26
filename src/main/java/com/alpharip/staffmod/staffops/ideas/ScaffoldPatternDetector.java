package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Summarizes supplied block placements for manual scaffold-pattern review; no automatic verdicts. */
public final class ScaffoldPatternDetector {
    public static final class PlacementSample {
        private final long timestampMillis;
        private final double playerX;
        private final double playerY;
        private final double playerZ;
        private final int blockX;
        private final int blockY;
        private final int blockZ;

        public PlacementSample(long timestampMillis, double playerX, double playerY, double playerZ,
                               int blockX, int blockY, int blockZ) {
            this.timestampMillis = timestampMillis;
            this.playerX = playerX;
            this.playerY = playerY;
            this.playerZ = playerZ;
            this.blockX = blockX;
            this.blockY = blockY;
            this.blockZ = blockZ;
        }
        public long getTimestampMillis() { return timestampMillis; }
        public double getPlayerX() { return playerX; }
        public double getPlayerY() { return playerY; }
        public double getPlayerZ() { return playerZ; }
        public int getBlockX() { return blockX; }
        public int getBlockY() { return blockY; }
        public int getBlockZ() { return blockZ; }
    }

    private final List<PlacementSample> samples = new ArrayList<PlacementSample>();
    public synchronized void add(PlacementSample sample) { if (sample != null) samples.add(sample); }
    public synchronized List<PlacementSample> getSamples() {
        return Collections.unmodifiableList(new ArrayList<PlacementSample>(samples));
    }
    public synchronized void clear() { samples.clear(); }
}