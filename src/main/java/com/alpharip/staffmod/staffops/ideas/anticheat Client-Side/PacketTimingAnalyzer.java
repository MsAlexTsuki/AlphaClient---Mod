package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records supplied packet timestamps and calculates inter-arrival timing. */
public class PacketTimingAnalyzer {
    private final List<Long> packetTimestampsMillis = new ArrayList<Long>();

    public void recordPacket(long timestampMillis) { packetTimestampsMillis.add(timestampMillis); }

    public List<Long> getPacketTimestampsMillis() {
        return Collections.unmodifiableList(new ArrayList<Long>(packetTimestampsMillis));
    }

    public long getLastInterArrivalMillis() {
        int size = packetTimestampsMillis.size();
        return size < 2 ? -1L : packetTimestampsMillis.get(size - 1) - packetTimestampsMillis.get(size - 2);
    }

    public void clear() { packetTimestampsMillis.clear(); }
}
