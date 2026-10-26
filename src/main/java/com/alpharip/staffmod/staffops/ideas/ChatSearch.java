package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Basic Java foundation for ChatSearch. Minecraft integration is intentionally deferred. */
public class ChatSearch {
    private boolean enabled = true;
    private final List<String> records = new ArrayList<String>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public void addRecord(String value) { if (value != null) records.add(value); }
    public List<String> getRecords() { return Collections.unmodifiableList(new ArrayList<String>(records)); }
    public void clearRecords() { records.clear(); }
}
