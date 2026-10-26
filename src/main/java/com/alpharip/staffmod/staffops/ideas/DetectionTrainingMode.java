package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Detection training state foundation; detection logic, safe validation, Minecraft integration, and tests remain to be implemented. */
public final class DetectionTrainingMode {
    private boolean enabled;
    private final List<String> entries = new ArrayList<String>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public void addEntry(String entry) { if (entry != null && !entry.trim().isEmpty()) entries.add(entry); }
    public List<String> getEntries() { return Collections.unmodifiableList(entries); }
    public void clearEntries() { entries.clear(); }
}
