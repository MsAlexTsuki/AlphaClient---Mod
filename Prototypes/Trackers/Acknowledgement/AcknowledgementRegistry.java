package com.alpharip.staffmod.trackers.acknowledgement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** In-memory registry; it does not provide persistence or authorization. */
public final class AcknowledgementRegistry {
    private final List<AcknowledgementEntry> entries =
        new ArrayList<AcknowledgementEntry>();
    private final int maximumEntries;
    private long nextId = 1L;

    public AcknowledgementRegistry(int maximumEntries) {
        if (maximumEntries < 1) {
            throw new IllegalArgumentException("maximumEntries must be at least 1.");
        }
        this.maximumEntries = maximumEntries;
    }

    public AcknowledgementEntry create(String createdBy, String subject, String details,
                                        long timestamp) {
        if (!AcknowledgementValidator.isValid(createdBy, subject, details)
                || timestamp < 0L || entries.size() >= maximumEntries) {
            return null;
        }
        AcknowledgementEntry entry = new AcknowledgementEntry(
            nextId++, createdBy, subject, details, timestamp);
        entries.add(entry);
        return entry;
    }

    public boolean acknowledge(long id, String staffName, long timestamp) {
        AcknowledgementEntry entry = findById(id);
        return entry != null && staffName != null && !staffName.trim().isEmpty()
            && entry.acknowledge(staffName, timestamp);
    }

    public AcknowledgementEntry findById(long id) {
        for (AcknowledgementEntry entry : entries) {
            if (entry.getId() == id) {
                return entry;
            }
        }
        return null;
    }

    public List<AcknowledgementEntry> getAll() {
        return Collections.unmodifiableList(
            new ArrayList<AcknowledgementEntry>(entries));
    }

    public List<AcknowledgementEntry> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAll();
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        List<AcknowledgementEntry> result = new ArrayList<AcknowledgementEntry>();
        for (AcknowledgementEntry entry : entries) {
            if (contains(entry.getSubject(), normalized)
                    || contains(entry.getDetails(), normalized)
                    || contains(entry.getCreatedBy(), normalized)
                    || contains(entry.getAcknowledgedBy(), normalized)) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public int getPendingCount() {
        int count = 0;
        for (AcknowledgementEntry entry : entries) {
            if (entry.getStatus() == AcknowledgementStatus.PENDING) {
                count++;
            }
        }
        return count;
    }

    public void clear() {
        entries.clear();
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
