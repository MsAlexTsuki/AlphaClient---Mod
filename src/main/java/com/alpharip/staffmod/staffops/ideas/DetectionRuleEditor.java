package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Maintains editable detection-rule drafts. */
public class DetectionRuleEditor {
    private final List<String> entries = new ArrayList<String>();
    public void add(String value) {
        if (value != null && !value.trim().isEmpty()) { entries.add(value.trim()); }
    }
    public List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<String>(entries));
    }
    public void clear() { entries.clear(); }
}