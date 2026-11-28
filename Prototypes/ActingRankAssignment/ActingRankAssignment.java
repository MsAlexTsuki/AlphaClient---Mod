package com.alpharip.staffmod.actingrankassignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main facade for temporary acting-rank records.
 * Legacy string-record methods remain available for compatibility with the idea stub.
 */
public final class ActingRankAssignment {
    private boolean enabled = true;
    private final List<String> records = new ArrayList<String>();
    private final ActingRankSettings settings = new ActingRankSettings();
    private final ActingRankRegistry registry = new ActingRankRegistry(settings.getMaximumEntries());
    private final ActingRankService service = new ActingRankService(registry, settings);

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        settings.setEnabled(enabled);
    }

    public void addRecord(String record) {
        if (record != null && !record.trim().isEmpty()) records.add(record);
    }

    public List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }

    public void clearRecords() {
        records.clear();
        registry.clear();
    }

    public ActingRankSettings getSettings() { return settings; }
    public ActingRankRegistry getRegistry() { return registry; }
    public ActingRankService getService() { return service; }
}
