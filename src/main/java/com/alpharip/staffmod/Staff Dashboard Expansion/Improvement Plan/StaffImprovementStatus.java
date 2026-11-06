package com.alpharip.staffmod.staffdashboard.improvement;

public enum StaffImprovementStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold");

    private final String displayName;

    StaffImprovementStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public StaffImprovementStatus next() {
        StaffImprovementStatus[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
