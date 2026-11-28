package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores non-negative gaps between packet observations. */
public class PacketGapAnalyzer {
    private final List<String> observations = new ArrayList<String>();
    public void add(String value) {
        if (value != null && !value.trim().isEmpty()) { observations.add(value.trim()); }
    }
    public List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<String>(observations));
    }
    public int size() { return observations.size(); }
    public void clear() { observations.clear(); }
}