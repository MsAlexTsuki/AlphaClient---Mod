package com.alpharip.staffdashboardexpansion.presence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local snapshot of staff presence observations; not an authoritative online roster. */
public final class StaffPresenceList {
    private final List<StaffPresenceEntry> entries = new ArrayList<StaffPresenceEntry>();

    public synchronized void update(String staffName, String status, long observedAtMillis) {
        if (staffName == null || staffName.trim().isEmpty()) return;
        for (int i = 0; i < entries.size(); i++) {
            if (staffName.trim().equalsIgnoreCase(entries.get(i).getStaffName())) {
                entries.set(i, new StaffPresenceEntry(staffName, status, observedAtMillis));
                return;
            }
        }
        entries.add(new StaffPresenceEntry(staffName, status, observedAtMillis));
    }

    public synchronized boolean remove(String staffName) {
        if (staffName == null) return false;
        for (int i = 0; i < entries.size(); i++) {
            if (staffName.equalsIgnoreCase(entries.get(i).getStaffName())) {
                entries.remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized void clear() { entries.clear(); }

    public synchronized List<StaffPresenceEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<StaffPresenceEntry>(entries));
    }

    public synchronized int size() { return entries.size(); }

    // TODO(Supabase): add a server-authorized shared source only when integration is approved.
}
