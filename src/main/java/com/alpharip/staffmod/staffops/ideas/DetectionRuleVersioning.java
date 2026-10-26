package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores successive versions of rule text. */
public class DetectionRuleVersioning {
    private final List<String> versions = new ArrayList<String>();
    public int save(String ruleText) {
        if (ruleText == null) { return -1; }
        versions.add(ruleText);
        return versions.size() - 1;
    }
    public String get(int index) {
        return index < 0 || index >= versions.size() ? null : versions.get(index);
    }
    public String rollbackTo(int index) { return get(index); }
    public List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<String>(versions));
    }
}