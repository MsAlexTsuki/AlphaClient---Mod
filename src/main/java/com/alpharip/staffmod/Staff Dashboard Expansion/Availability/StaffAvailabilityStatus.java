package com.alpharip.staffdashboardexpansion.availability;

/** Availability states shown on the staff board. */
public enum StaffAvailabilityStatus {
    AVAILABLE("Available"),
    BUSY("Busy"),
    AWAY("Away"),
    UNAVAILABLE("Unavailable");

    private final String displayName;
    StaffAvailabilityStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public StaffAvailabilityStatus next() {
        StaffAvailabilityStatus[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
