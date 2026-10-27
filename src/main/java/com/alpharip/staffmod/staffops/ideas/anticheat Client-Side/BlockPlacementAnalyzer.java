package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied block-placement events for review; no world or packet access is performed. */
public final class BlockPlacementAnalyzer {
    public static final class Placement {
        private final long timestampMillis;
        private final int x;
        private final int y;
        private final int z;
        private final String blockType;

        public Placement(long timestampMillis, int x, int y, int z, String blockType) {
            this.timestampMillis = timestampMillis;
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockType = blockType == null ? "" : blockType.trim();
        }
        public long getTimestampMillis() { return timestampMillis; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public String getBlockType() { return blockType; }
    }

    private final List<Placement> placements = new ArrayList<Placement>();
    public synchronized void add(Placement placement) { if (placement != null) placements.add(placement); }
    public synchronized List<Placement> getPlacements() {
        return Collections.unmodifiableList(new ArrayList<Placement>(placements));
    }
    public synchronized void clear() { placements.clear(); }
}