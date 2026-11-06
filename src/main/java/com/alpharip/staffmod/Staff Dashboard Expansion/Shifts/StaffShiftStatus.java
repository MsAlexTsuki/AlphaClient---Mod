package com.alpharip.staffdashboardexpansion.shifts;

/** Lifecycle state of a planned staff shift. */
public enum StaffShiftStatus {
    SCHEDULED("Scheduled"), ACTIVE("Active"), COMPLETED("Completed"), CANCELLED("Cancelled");
    private final String label;
    StaffShiftStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    public StaffShiftStatus next() {
        StaffShiftStatus[] values = values();
        return values()[(ordinal() + 1) % values.length];
    }
}
