package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Foundational data holder for AlphaTrail.
 * UI, persistence, permission enforcement, and live Minecraft integration are not connected.
 */
public class AlphaTrail {
    private boolean enabled = true;
    private final List<String> records = new ArrayList<String>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void addRecord(String record) {
        if (record != null && !record.trim().isEmpty()) records.add(record);
    }
    public List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }
    public void clearRecords() { records.clear(); }
}
