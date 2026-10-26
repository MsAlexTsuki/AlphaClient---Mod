package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reversible staff action history foundation; feature wiring, persistence, and tests remain to be implemented. */
public final class StaffUndoSystem {
    private boolean enabled;
    private final List<String> entries = new ArrayList<String>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public void addEntry(String value) { if (value != null && !value.trim().isEmpty()) entries.add(value); }
    public List<String> getEntries() { return Collections.unmodifiableList(entries); }
    public void clearEntries() { entries.clear(); }
}
