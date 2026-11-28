package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores references to evidence reserved for senior review.
 * This is a foundation component; application UI, persistence, and live event
 * integration must be connected separately.
 */
public final class SeniorEvidenceVault {
    private final List<String> records = new ArrayList<String>();
    private boolean enabled = true;

    public void addRecord(String record) {
        if (record != null && !record.trim().isEmpty()) records.add(record);
    }

    public List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void clear() { records.clear(); }
}
