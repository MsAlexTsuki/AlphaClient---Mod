package com.alpharip.staffdashboardexpansion.shifts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone in-memory shift-start checklist prototype.
 *
 * This class has no Alpha Client Core, Supabase, network, or disk dependency.
 * TODO: Connect to an approved staff identity and shared persistence layer only
 * when production integration is explicitly designed.
 */
public final class ShiftStartChecklist {
    public static final String NAME = "Shift Start Checklist";

    private final Map<String, ChecklistItem> items =
        new LinkedHashMap<String, ChecklistItem>();
    private long nextId = 1L;

    public ShiftStartChecklist() {
        add("Review staff dashboard", "Check the dashboard for new information.",
            ChecklistCategory.GENERAL);
        add("Review open staff queue tasks", "Check priority and ownership of open tasks.",
            ChecklistCategory.GENERAL);
        add("Review pending handoffs", "Check handoffs that need to be accepted or continued.",
            ChecklistCategory.COMMUNICATION);
        add("Check staff notices", "Read current staff announcements and instructions.",
            ChecklistCategory.COMMUNICATION);
        add("Review recent player reports", "Check reports that still require attention.",
            ChecklistCategory.MODERATION);
        add("Confirm duty status", "Set the correct availability before taking assignments.",
            ChecklistCategory.AVAILABILITY);
    }

    public synchronized ChecklistItem add(String title, String description,
                                          ChecklistCategory category) {
        String id = "shift-check-" + nextId++;
        ChecklistItem item = new ChecklistItem(id, title, description, category);
        items.put(id, item);
        return item;
    }

    public synchronized boolean remove(String id) {
        return id != null && items.remove(id) != null;
    }

    public synchronized boolean setCompleted(String id, boolean completed) {
        ChecklistItem item = items.get(id);
        if (item == null) {
            return false;
        }
        item.setCompleted(completed);
        return true;
    }

    public synchronized boolean update(String id, String title, String description,
                                        ChecklistCategory category) {
        ChecklistItem item = items.get(id);
        if (item == null) {
            return false;
        }
        item.update(title, description, category);
        return true;
    }

    public synchronized void reset() {
        for (ChecklistItem item : items.values()) {
            item.setCompleted(false);
        }
    }

    public synchronized void clear() {
        items.clear();
    }

    public synchronized List<ChecklistItem> getItems() {
        return Collections.unmodifiableList(
            new ArrayList<ChecklistItem>(items.values()));
    }

    public synchronized List<ChecklistItem> getItems(ChecklistCategory category) {
        List<ChecklistItem> result = new ArrayList<ChecklistItem>();
        for (ChecklistItem item : items.values()) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int getCompletedCount() {
        int count = 0;
        for (ChecklistItem item : items.values()) {
            if (item.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public synchronized int getRemainingCount() {
        return items.size() - getCompletedCount();
    }

    public synchronized int getTotalCount() {
        return items.size();
    }

    public synchronized boolean isComplete() {
        return !items.isEmpty() && getRemainingCount() == 0;
    }

    public synchronized double getProgress() {
        return items.isEmpty() ? 0.0D : (double) getCompletedCount() / items.size();
    }
}
