package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Investigation workspace overlay foundation; integration and verification are still required. */
public final class InvestigationOverlay {
    private boolean enabled;
    private final List<String> records = new ArrayList<String>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public void addRecord(String value) { if (value != null && !value.trim().isEmpty()) records.add(value); }
    public List<String> getRecords() { return Collections.unmodifiableList(records); }
    public void clearRecords() { records.clear(); }
}
