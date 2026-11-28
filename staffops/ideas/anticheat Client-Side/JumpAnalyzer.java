package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied jump measurements for analysis; does not monitor live movement. */
public final class JumpAnalyzer {
    public static final class Jump {
        private final long startMillis;
        private final long endMillis;
        private final double height;

        public Jump(long startMillis, long endMillis, double height) {
            this.startMillis = startMillis;
            this.endMillis = endMillis;
            this.height = height;
        }
        public long getStartMillis() { return startMillis; }
        public long getEndMillis() { return endMillis; }
        public double getHeight() { return height; }
        public long getDurationMillis() { return Math.max(0L, endMillis - startMillis); }
    }

    private final List<Jump> jumps = new ArrayList<Jump>();
    public synchronized void add(Jump jump) { if (jump != null) jumps.add(jump); }
    public synchronized List<Jump> getJumps() {
        return Collections.unmodifiableList(new ArrayList<Jump>(jumps));
    }
    public synchronized void clear() { jumps.clear(); }
}