package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Produces a read-only view of a supplied rule draft. */
public class DetectionRulePreview {
    private final List<String> entries = new ArrayList<String>();
    public void add(String value) {
        if (value != null && !value.trim().isEmpty()) { entries.add(value.trim()); }
    }
    public List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<String>(entries));
    }
    public void clear() { entries.clear(); }
}