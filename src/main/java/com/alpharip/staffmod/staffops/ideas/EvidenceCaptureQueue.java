package com.alpharip.staffmod.staffops.ideas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Local queue of planned evidence captures; does not take screenshots itself. */
public final class EvidenceCaptureQueue {
    private final List<QueueItem> items = new ArrayList<QueueItem>();
    private long nextId = 1L;

    public QueueItem enqueue(String description, long requestedAtMillis) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        QueueItem item = new QueueItem(nextId++, description.trim(), requestedAtMillis);
        items.add(item);
        return item;
    }

    public List<QueueItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<QueueItem>(items));
    }

    public static final class QueueItem {
        private final long id;
        private final String description;
        private final long requestedAtMillis;
        private boolean completed;

        private QueueItem(long id, String description, long requestedAtMillis) {
            this.id = id;
            this.description = description;
            this.requestedAtMillis = requestedAtMillis;
        }

        public long getId() { return id; }
        public String getDescription() { return description; }
        public long getRequestedAtMillis() { return requestedAtMillis; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}