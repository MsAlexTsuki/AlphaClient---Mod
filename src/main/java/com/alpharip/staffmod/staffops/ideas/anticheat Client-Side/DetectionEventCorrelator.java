package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Groups event references by correlation key. */
public class DetectionEventCorrelator {
    private final List<String> entries = new ArrayList<String>();
    public boolean add(String value) {
        if (value == null) { return false; }
        entries.add(value);
        return true;
    }
    public List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<String>(entries));
    }
    public void clear() { entries.clear(); }
}