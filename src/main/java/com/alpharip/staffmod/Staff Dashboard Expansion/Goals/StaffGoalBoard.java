package com.alpharip.staffdashboardexpansion.goals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Standalone, in-memory staff goal board.
 *
 * No Alpha Client Core, Supabase, network, or disk persistence dependency.
 * TODO: Connect staff identity, permission checks, and shared persistence only
 * when a production integration is explicitly designed.
 */
public final class StaffGoalBoard {
    public static final String NAME = "Staff Goal Board";
    public static final String CATEGORY = "Management";
    public static final String STATUS = "PROTOTYPE";
    public static final String REQUIRED_ACCESS = "Unconfirmed";

    private final Map<String, StaffGoal> goals =
        new LinkedHashMap<String, StaffGoal>();
    private long nextId = 1L;

    /** Preserves the original idea API while using the expanded goal model. */
    public synchronized void add(String id, String team, String title, long dueAt) {
        addGoal(id, team, title, "", dueAt);
    }

    public synchronized StaffGoal addGoal(String id, String team, String title,
                                           String description, long dueAt) {
        String safeId = requireText(id, "id");
        if (goals.containsKey(safeId)) {
            throw new IllegalArgumentException("A goal with this id already exists.");
        }
        if (dueAt < 0L) {
            throw new IllegalArgumentException("dueAt must be non-negative.");
        }

        long now = System.currentTimeMillis();
        StaffGoal goal = new StaffGoal(safeId, requireText(team, "team"),
            requireText(title, "title"), description == null ? "" : description.trim(),
            dueAt, now);
        goals.put(safeId, goal);
        return goal;
    }

    public synchronized StaffGoal createGoal(String team, String title,
                                              String description, long dueAt) {
        String id;
        do {
            id = "goal-" + nextId++;
        } while (goals.containsKey(id));
        return addGoal(id, team, title, description, dueAt);
    }

    /** Preserves the original string-based status API. */
    public synchronized void setStatus(String id, String status) {
        String normalized = requireText(status, "status").toUpperCase(Locale.ROOT)
            .replace(' ', '_');
        try {
            setStatus(id, StaffGoalStatus.valueOf(normalized));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown goal status: " + status);
        }
    }

    public synchronized boolean setStatus(String id, StaffGoalStatus status) {
        StaffGoal goal = goals.get(requireText(id, "id"));
        if (goal == null || status == null) {
            return false;
        }
        goal.setStatus(status, System.currentTimeMillis());
        return true;
    }

    public synchronized boolean setProgress(String id, int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100.");
        }
        StaffGoal goal = goals.get(requireText(id, "id"));
        if (goal == null) {
            return false;
        }
        goal.setProgress(progress, System.currentTimeMillis());
        return true;
    }

    public synchronized boolean removeGoal(String id) {
        return goals.remove(requireText(id, "id")) != null;
    }

    public synchronized StaffGoal getGoal(String id) {
        return goals.get(requireText(id, "id"));
    }

    public synchronized List<StaffGoal> snapshot() {
        return snapshot(null, null);
    }

    public synchronized List<StaffGoal> snapshot(String team, StaffGoalStatus status) {
        List<StaffGoal> result = new ArrayList<StaffGoal>();
        for (StaffGoal goal : goals.values()) {
            boolean teamMatches = team == null || team.trim().isEmpty()
                || goal.getTeam().equalsIgnoreCase(team.trim());
            boolean statusMatches = status == null || goal.getStatus() == status;
            if (teamMatches && statusMatches) {
                result.add(goal);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<String> getTeams() {
        List<String> result = new ArrayList<String>();
        for (StaffGoal goal : goals.values()) {
            boolean found = false;
            for (String team : result) {
                if (team.equalsIgnoreCase(goal.getTeam())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(goal.getTeam());
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int count(StaffGoalStatus status) {
        int count = 0;
        for (StaffGoal goal : goals.values()) {
            if (goal.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public synchronized int countOverdue(long now) {
        int count = 0;
        for (StaffGoal goal : goals.values()) {
            if (goal.isOverdue(now)) {
                count++;
            }
        }
        return count;
    }

    public synchronized int size() {
        return goals.size();
    }

    public synchronized void clear() {
        goals.clear();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
