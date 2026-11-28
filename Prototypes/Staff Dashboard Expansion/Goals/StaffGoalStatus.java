package com.alpharip.staffdashboardexpansion.goals;

/** Lifecycle states supported by the local Staff Goal Board prototype. */
public enum StaffGoalStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    StaffGoalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public StaffGoalStatus next() {
        StaffGoalStatus[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
