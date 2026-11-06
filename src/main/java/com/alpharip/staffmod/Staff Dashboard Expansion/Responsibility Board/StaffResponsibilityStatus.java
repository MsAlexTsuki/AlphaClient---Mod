package com.alpharip.staffmod.staffdashboard.responsibility;

public enum StaffResponsibilityStatus {
    UNASSIGNED("Unassigned"), ASSIGNED("Assigned"), IN_PROGRESS("In Progress"),
    COMPLETED("Completed"), ON_HOLD("On Hold");
    private final String displayName;
    StaffResponsibilityStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
    public StaffResponsibilityStatus next() {
        StaffResponsibilityStatus[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
