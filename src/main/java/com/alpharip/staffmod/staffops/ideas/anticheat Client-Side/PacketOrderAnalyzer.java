package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Keeps caller-supplied packet sequence identifiers for ordering checks. */
public class PacketOrderAnalyzer {
    private final List<Long> sequenceNumbers = new ArrayList<Long>();

    public void recordSequence(long sequenceNumber) { sequenceNumbers.add(Long.valueOf(sequenceNumber)); }

    public List<Long> getSequenceNumbers() {
        return Collections.unmodifiableList(new ArrayList<Long>(sequenceNumbers));
    }

    public boolean isNonDecreasing() {
        for (int i = 1; i < sequenceNumbers.size(); i++) {
            if (sequenceNumbers.get(i).longValue() < sequenceNumbers.get(i - 1).longValue()) return false;
        }
        return true;
    }

    public void clear() { sequenceNumbers.clear(); }
}
