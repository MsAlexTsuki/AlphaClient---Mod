package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied block-break events; does not inspect a live world or validate actions. */
public final class BlockBreakAnalyzer {
    public static final class BreakEvent {
        private final long timestampMillis;
        private final int x;
        private final int y;
        private final int z;
        private final String blockType;
        private final long durationMillis;

        public BreakEvent(long timestampMillis, int x, int y, int z,
                          String blockType, long durationMillis) {
            this.timestampMillis = timestampMillis;
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockType = blockType == null ? "" : blockType.trim();
            this.durationMillis = Math.max(0L, durationMillis);
        }
        public long getTimestampMillis() { return timestampMillis; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public String getBlockType() { return blockType; }
        public long getDurationMillis() { return durationMillis; }
    }

    private final List<BreakEvent> events = new ArrayList<BreakEvent>();
    public synchronized void add(BreakEvent event) { if (event != null) events.add(event); }
    public synchronized List<BreakEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<BreakEvent>(events));
    }
    public synchronized void clear() { events.clear(); }
}