package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Foundation class for ChatTabs; runtime integration is not implemented here. */
public class ChatTabs {
    private boolean enabled = true;
    private final List<String> records = new ArrayList<String>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void addRecord(String record) { if (record != null) records.add(record); }
    public List<String> getRecords() { return Collections.unmodifiableList(new ArrayList<String>(records)); }
    public void clearRecords() { records.clear(); }
}
