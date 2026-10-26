package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local list of staff presence entries supplied by an existing client-visible source.
 * Entries are not proof of complete or authoritative staff presence.
 */
public final class StaffPresenceList {
    private final List<PresenceEntry> entries = new ArrayList<PresenceEntry>();

    public void update(String staffName, String status, long observedAtMillis) {
        if (staffName == null || staffName.trim().isEmpty()) {
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            if (staffName.equalsIgnoreCase(entries.get(i).getStaffName())) {
                entries.set(i, new PresenceEntry(staffName, status, observedAtMillis));
                return;
            }
        }
        entries.add(new PresenceEntry(staffName, status, observedAtMillis));
    }

    public List<PresenceEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<PresenceEntry>(entries));
    }

    public static final class PresenceEntry {
        private final String staffName;
        private final String status;
        private final long observedAtMillis;

        public PresenceEntry(String staffName, String status, long observedAtMillis) {
            this.staffName = staffName;
            this.status = status == null ? "Unknown" : status;
            this.observedAtMillis = observedAtMillis;
        }

        public String getStaffName() { return staffName; }
        public String getStatus() { return status; }
        public long getObservedAtMillis() { return observedAtMillis; }
    }

    // TODO(Supabase): define server-authorized presence data if a shared list is needed.
}