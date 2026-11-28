package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

public enum StaffReviewStatus {
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    StaffReviewStatus(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    public StaffReviewStatus next() {
        StaffReviewStatus[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
