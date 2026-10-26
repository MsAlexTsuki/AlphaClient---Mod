package com.alpharip.staffmod.staffops.ideas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks senior assignments, ownership, priority, due date and completion.
 *
 * This is an in-memory workflow model; it does not notify staff or enforce rank.
 * TODO(Supabase): persist assignments, audit changes and enforce senior-only
 * write permissions before connecting this to a live staff workflow.
 * Compatible with Java 8.
 */
public class SeniorAssignmentBoard {
    public enum Priority { LOW, NORMAL, HIGH, CRITICAL }
    public enum Status { OPEN, IN_PROGRESS, BLOCKED, DONE, CANCELLED }

    public static final class Assignment {
        private final String id;
        private final String title;
        private final String assigneeId;
        private final Priority priority;
        private final LocalDateTime dueAt;
        private final Status status;

        private Assignment(String id, String title, String assigneeId, Priority priority,
                           LocalDateTime dueAt, Status status) {
            this.id = id;
            this.title = title;
            this.assigneeId = assigneeId;
            this.priority = priority;
            this.dueAt = dueAt;
            this.status = status;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAssigneeId() { return assigneeId; }
        public Priority getPriority() { return priority; }
        public LocalDateTime getDueAt() { return dueAt; }
        public Status getStatus() { return status; }
        public boolean isOpen() { return status != Status.DONE && status != Status.CANCELLED; }
        public boolean isOverdue(LocalDateTime now) {
            return dueAt != null && now != null && dueAt.isBefore(now) && isOpen();
        }
    }

    private final Map<String, Assignment> assignments = new LinkedHashMap<String, Assignment>();

    public void add(String id, String title, String assigneeId, Priority priority, LocalDateTime dueAt) {
        String key = requireText(id, "id");
        if (assignments.containsKey(key)) {
            throw new IllegalArgumentException("Assignment already exists: " + key);
        }
        assignments.put(key, new Assignment(key, requireText(title, "title"),
                assigneeId == null || assigneeId.trim().isEmpty() ? null : assigneeId.trim(),
                priority == null ? Priority.NORMAL : priority, dueAt, Status.OPEN));
    }

    public void assign(String id, String assigneeId) {
        Assignment current = requireAssignment(id);
        assignments.put(current.getId(), new Assignment(current.getId(), current.getTitle(),
                requireText(assigneeId, "assigneeId"), current.getPriority(),
                current.getDueAt(), current.getStatus()));
    }

    public void setStatus(String id, Status status) {
        Assignment current = requireAssignment(id);
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        assignments.put(current.getId(), new Assignment(current.getId(), current.getTitle(),
                current.getAssigneeId(), current.getPriority(), current.getDueAt(), status));
    }

    public List<Assignment> getAll() {
        return Collections.unmodifiableList(new ArrayList<Assignment>(assignments.values()));
    }

    public List<Assignment> getOpen() {
        List<Assignment> result = new ArrayList<Assignment>();
        for (Assignment assignment : assignments.values()) {
            if (assignment.isOpen()) {
                result.add(assignment);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<Assignment> getOverdue(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null.");
        }
        List<Assignment> result = new ArrayList<Assignment>();
        for (Assignment assignment : assignments.values()) {
            if (assignment.isOverdue(now)) {
                result.add(assignment);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private Assignment requireAssignment(String id) {
        String key = requireText(id, "id");
        Assignment assignment = assignments.get(key);
        if (assignment == null) {
            throw new IllegalArgumentException("Unknown assignment: " + key);
        }
        return assignment;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
