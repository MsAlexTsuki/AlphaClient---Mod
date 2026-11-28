package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Foundation for Estimated Server TPS.
 * Feature-specific Minecraft integration, persistence, permissions, and UI wiring remain to be implemented and tested.
 */
public final class EstimatedServerTps {
    private boolean enabled;
    private final List<String> records = new ArrayList<String>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addRecord(String record) {
        if (record != null && !record.trim().isEmpty()) {
            records.add(record);
        }
    }

    public List<String> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public void clearRecords() {
        records.clear();
    }
}
