package com.alpharip.staffdashboardexpansion.staffdutymode;

/** Available local states in the Staff Duty Mode prototype. */
public enum StaffDutyState {
    OFF_DUTY("Off Duty", 0xFF777777),
    ON_DUTY("On Duty", 0xFF55CC77),
    BUSY("Busy", 0xFFFFC84A);

    private final String displayName;
    private final int color;

    StaffDutyState(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public boolean isOnDuty() {
        return this != OFF_DUTY;
    }

    public boolean canReceiveAssignments() {
        return this == ON_DUTY;
    }
}
