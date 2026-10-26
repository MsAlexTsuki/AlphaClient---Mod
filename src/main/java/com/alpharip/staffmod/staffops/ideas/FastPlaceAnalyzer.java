package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores supplied block-placement intervals for later review. */
public class FastPlaceAnalyzer {
    private final List<Long> placementTimestamps = new ArrayList<Long>();

    public void recordPlacement(long timestampMillis) { placementTimestamps.add(timestampMillis); }

    public List<Long> getPlacementTimestamps() {
        return Collections.unmodifiableList(new ArrayList<Long>(placementTimestamps));
    }

    public long getLastPlacementIntervalMillis() {
        int size = placementTimestamps.size();
        return size < 2 ? -1L : placementTimestamps.get(size - 1) - placementTimestamps.get(size - 2);
    }

    public void clear() { placementTimestamps.clear(); }
}
