package com.alpharip.staffmod.staffdashboardexpansion.staffqueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Standalone, in-memory staff task queue prototype.
 *
 * No Alpha Client Core, Supabase, network, or disk persistence dependency.
 * TODO: Connect approved staff identity/permissions and remote persistence
 * only when the production integration is explicitly designed.
 */
public final class StaffQueue {
    private final List<StaffQueueEntry> entries = new ArrayList<StaffQueueEntry>();
    private long nextId = 1L;
    private int maximumEntries = 100;

    public synchronized StaffQueueEntry addTask(String title, String description,
                                                  String createdBy, StaffQueuePriority priority) {
        String safeTitle = requireText(title, "title");
        String safeCreator = requireText(createdBy, "createdBy");
        if (entries.size() >= maximumEntries) {
            throw new IllegalStateException("The staff queue has reached its capacity.");
        }
        StaffQueuePriority safePriority = priority == null ? StaffQueuePriority.NORMAL : priority;
        long now = System.currentTimeMillis();
        StaffQueueEntry entry = new StaffQueueEntry(nextId++, safeTitle,
            description == null ? "" : description.trim(), safeCreator, safePriority, now);
        entries.add(entry);
        return entry;
    }

    public synchronized boolean assign(long taskId, String staffName) {
        StaffQueueEntry entry = find(taskId);
        if (entry == null || entry.getStatus() == StaffQueueStatus.COMPLETED
                || entry.getStatus() == StaffQueueStatus.CANCELLED) {
            return false;
        }
        entry.assignTo(requireText(staffName, "staffName"), System.currentTimeMillis());
        return true;
    }

    public synchronized boolean claim(long taskId, String staffName) {
        StaffQueueEntry entry = find(taskId);
        if (entry == null || entry.getStatus() != StaffQueueStatus.OPEN) {
            return false;
        }
        entry.assignTo(requireText(staffName, "staffName"), System.currentTimeMillis());
        entry.setStatus(StaffQueueStatus.IN_PROGRESS, System.currentTimeMillis());
        return true;
    }

    public synchronized boolean setStatus(long taskId, StaffQueueStatus status) {
        StaffQueueEntry entry = find(taskId);
        if (entry == null || status == null) {
            return false;
        }
        entry.setStatus(status, System.currentTimeMillis());
        return true;
    }

    public synchronized boolean complete(long taskId) {
        return setStatus(taskId, StaffQueueStatus.COMPLETED);
    }

    public synchronized boolean cancel(long taskId) {
        return setStatus(taskId, StaffQueueStatus.CANCELLED);
    }

    public synchronized StaffQueueEntry getTask(long taskId) {
        return find(taskId);
    }

    public synchronized List<StaffQueueEntry> getTasks() {
        List<StaffQueueEntry> result = new ArrayList<StaffQueueEntry>(entries);
        Collections.sort(result, new Comparator<StaffQueueEntry>() {
            @Override
            public int compare(StaffQueueEntry left, StaffQueueEntry right) {
                int priorityOrder = Integer.compare(
                    right.getPriority().getWeight(), left.getPriority().getWeight());
                if (priorityOrder != 0) {
                    return priorityOrder;
                }
                return Long.compare(left.getCreatedAt(), right.getCreatedAt());
            }
        });
        return Collections.unmodifiableList(result);
    }

    public synchronized List<StaffQueueEntry> getTasks(StaffQueueStatus status) {
        List<StaffQueueEntry> result = new ArrayList<StaffQueueEntry>();
        for (StaffQueueEntry entry : getTasks()) {
            if (entry.getStatus() == status) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int getOpenTaskCount() {
        int count = 0;
        for (StaffQueueEntry entry : entries) {
            if (entry.getStatus() == StaffQueueStatus.OPEN
                    || entry.getStatus() == StaffQueueStatus.IN_PROGRESS) {
                count++;
            }
        }
        return count;
    }

    public synchronized boolean remove(long taskId) {
        StaffQueueEntry entry = find(taskId);
        return entry != null && entries.remove(entry);
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized int getMaximumEntries() {
        return maximumEntries;
    }

    public synchronized void setMaximumEntries(int maximumEntries) {
        if (maximumEntries < 1) {
            throw new IllegalArgumentException("maximumEntries must be at least 1.");
        }
        if (maximumEntries < entries.size()) {
            throw new IllegalStateException("Maximum capacity cannot be lower than current queue size.");
        }
        this.maximumEntries = maximumEntries;
    }

    private StaffQueueEntry find(long taskId) {
        for (StaffQueueEntry entry : entries) {
            if (entry.getId() == taskId) {
                return entry;
            }
        }
        return null;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
