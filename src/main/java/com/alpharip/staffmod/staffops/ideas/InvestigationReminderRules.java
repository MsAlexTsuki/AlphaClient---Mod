package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local reminder definitions; scheduling and notifications are not implemented here. */
public final class InvestigationReminderRules {
    public static final class Reminder {
        private final String id;
        private final String message;
        private final long dueAtMillis;
        private final boolean enabled;

        public Reminder(String id, String message, long dueAtMillis, boolean enabled) {
            this.id = id == null ? "" : id;
            this.message = message == null ? "" : message;
            this.dueAtMillis = dueAtMillis;
            this.enabled = enabled;
        }
        public String getId() { return id; }
        public String getMessage() { return message; }
        public long getDueAtMillis() { return dueAtMillis; }
        public boolean isEnabled() { return enabled; }
    }

    private final List<Reminder> reminders = new ArrayList<Reminder>();

    public synchronized void add(Reminder reminder) { if (reminder != null) reminders.add(reminder); }
    public synchronized boolean remove(String id) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId().equals(id)) { reminders.remove(i); return true; }
        }
        return false;
    }
    public synchronized List<Reminder> getAll() {
        return Collections.unmodifiableList(new ArrayList<Reminder>(reminders));
    }
}