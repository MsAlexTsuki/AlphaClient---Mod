package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Foundation for historical punishment records.
 * Basic Java foundation only; game hooks, persistence, permissions, UI integration,
 * and runtime behavior remain to be implemented and tested.
 */
public class PunishmentHistory {
    private boolean enabled = true;
    private final List<String> records = new ArrayList<String>();
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public void addRecord(String value) { if (value != null && !value.trim().isEmpty()) records.add(value); }
    public List<String> getRecords() { return Collections.unmodifiableList(new ArrayList<String>(records)); }
    public void clearRecords() { records.clear(); }
}
