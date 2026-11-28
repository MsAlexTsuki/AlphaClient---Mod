package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Holds local investigation notification items; no push or system notifications are sent. */
public final class InvestigationNotifications {
    public static final class Notification {
        private final String title;
        private final String message;
        private final long timestampMillis;
        private boolean read;

        public Notification(String title, String message, long timestampMillis) {
            this.title = title == null ? "" : title.trim();
            this.message = message == null ? "" : message.trim();
            this.timestampMillis = timestampMillis;
        }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public long getTimestampMillis() { return timestampMillis; }
        public synchronized boolean isRead() { return read; }
        public synchronized void markRead() { read = true; }
    }

    private final List<Notification> items = new ArrayList<Notification>();
    public synchronized void add(Notification item) { if (item != null) items.add(item); }
    public synchronized List<Notification> getItems() {
        return Collections.unmodifiableList(new ArrayList<Notification>(items));
    }
}