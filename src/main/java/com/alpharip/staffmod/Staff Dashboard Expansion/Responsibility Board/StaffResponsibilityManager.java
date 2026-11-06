package com.alpharip.staffmod.staffdashboard.responsibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** In-memory manager for the standalone prototype. */
public class StaffResponsibilityManager {
    private final List<StaffResponsibility> responsibilities = new ArrayList<StaffResponsibility>();
    private int nextId = 1;

    public StaffResponsibility create(String title, String description, String assignee,
                                      StaffResponsibilityPriority priority) {
        StaffResponsibility item = new StaffResponsibility(nextId++, title, description, assignee, priority);
        responsibilities.add(item);
        return item;
    }

    public StaffResponsibility get(int id) {
        for (StaffResponsibility item : responsibilities) if (item.getId() == id) return item;
        return null;
    }

    public boolean remove(int id) {
        for (int i = 0; i < responsibilities.size(); i++) {
            if (responsibilities.get(i).getId() == id) {
                responsibilities.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<StaffResponsibility> getAll() {
        return Collections.unmodifiableList(new ArrayList<StaffResponsibility>(responsibilities));
    }

    public List<StaffResponsibility> getByAssignee(String assignee) {
        List<StaffResponsibility> result = new ArrayList<StaffResponsibility>();
        String expected = assignee == null ? "" : assignee.trim();
        for (StaffResponsibility item : responsibilities) {
            if (item.getAssignee().equalsIgnoreCase(expected)) result.add(item);
        }
        return Collections.unmodifiableList(result);
    }

    public List<StaffResponsibility> getByStatus(StaffResponsibilityStatus status) {
        List<StaffResponsibility> result = new ArrayList<StaffResponsibility>();
        for (StaffResponsibility item : responsibilities) if (item.getStatus() == status) result.add(item);
        return Collections.unmodifiableList(result);
    }

    public List<StaffResponsibility> getByPriority(StaffResponsibilityPriority priority) {
        List<StaffResponsibility> result = new ArrayList<StaffResponsibility>();
        for (StaffResponsibility item : responsibilities) if (item.getPriority() == priority) result.add(item);
        return Collections.unmodifiableList(result);
    }

    public int count(StaffResponsibilityStatus status) { return getByStatus(status).size(); }
    public int size() { return responsibilities.size(); }
    public void clear() { responsibilities.clear(); }
}
