package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Evaluates supplied packet counts against a caller-defined burst threshold. */
public class PacketBurstDetector {
    private final List<Integer> observedPacketCounts = new ArrayList<Integer>();

    public void recordWindowPacketCount(int packetCount) {
        if (packetCount >= 0) observedPacketCounts.add(Integer.valueOf(packetCount));
    }

    public List<Integer> getObservedPacketCounts() {
        return Collections.unmodifiableList(new ArrayList<Integer>(observedPacketCounts));
    }

    public boolean exceedsThreshold(int packetCount, int threshold) {
        return packetCount >= 0 && threshold >= 0 && packetCount > threshold;
    }

    public void clear() { observedPacketCounts.clear(); }
}
