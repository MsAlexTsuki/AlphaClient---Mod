package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores explicitly supplied click timestamps for review; it does not listen for mouse input. */
public final class ClickAnalyzer {
    private final List<Long> clickTimestamps = new ArrayList<Long>();

    public synchronized void addClick(long timestampMillis) {
        clickTimestamps.add(timestampMillis);
    }
    public synchronized List<Long> getClickTimestamps() {
        return Collections.unmodifiableList(new ArrayList<Long>(clickTimestamps));
    }
    public synchronized double averageClicksPerSecond() {
        if (clickTimestamps.size() < 2) return 0.0D;
        long first = clickTimestamps.get(0);
        long last = clickTimestamps.get(clickTimestamps.size() - 1);
        long elapsed = last - first;
        if (elapsed <= 0L) return 0.0D;
        return (clickTimestamps.size() - 1) * 1000.0D / elapsed;
    }
    public synchronized void clear() { clickTimestamps.clear(); }
}