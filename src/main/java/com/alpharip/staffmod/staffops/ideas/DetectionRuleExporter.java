package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exports stored detection rule text. */
public class DetectionRuleExporter {
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