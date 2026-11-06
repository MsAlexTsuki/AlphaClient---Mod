package com.alpharip.staffdashboardexpansion.availability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Local in-memory staff availability board.
 * TODO: Connect to the staff identity and permission layer when integrated.
 * TODO: Add shared persistence/synchronization outside this isolated prototype.
 */
public final class StaffAvailabilityBoard {
    private boolean enabled = true;
    private final Map<String, StaffAvailabilityEntry> entries = new LinkedHashMap<String, StaffAvailabilityEntry>();

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; }

    public synchronized StaffAvailabilityEntry setAvailability(String staffName, StaffAvailabilityStatus status, String note) {
        String key = key(staffName);
        StaffAvailabilityEntry entry = entries.get(key);
        if (entry == null) {
            entry = new StaffAvailabilityEntry(staffName, status, note);
            entries.put(key, entry);
        } else {
            entry.setStatus(status);
            entry.setNote(note);
        }
        return entry;
    }

    public synchronized boolean updateStatus(String staffName, StaffAvailabilityStatus status) {
        StaffAvailabilityEntry entry = entries.get(key(staffName));
        if (entry == null) return false;
        entry.setStatus(status);
        return true;
    }

    public synchronized boolean remove(String staffName) {
        return entries.remove(key(staffName)) != null;
    }

    public synchronized StaffAvailabilityEntry get(String staffName) {
        return entries.get(key(staffName));
    }

    public synchronized List<StaffAvailabilityEntry> snapshot() {
        return Collections.unmodifiableList(new ArrayList<StaffAvailabilityEntry>(entries.values()));
    }

    public synchronized List<StaffAvailabilityEntry> filter(StaffAvailabilityStatus status) {
        List<StaffAvailabilityEntry> result = new ArrayList<StaffAvailabilityEntry>();
        for (StaffAvailabilityEntry entry : entries.values()) {
            if (status == null || entry.getStatus() == status) result.add(entry);
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int count(StaffAvailabilityStatus status) {
        int count = 0;
        for (StaffAvailabilityEntry entry : entries.values()) if (entry.getStatus() == status) count++;
        return count;
    }

    public synchronized void clear() { entries.clear(); }

    private static String key(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Staff name is required");
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
