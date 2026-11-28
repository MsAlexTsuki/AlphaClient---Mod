package com.alpharip.staffdashboardexpansion.shifts;

/** Categories used to organize the local staff shift-start checklist. */
public enum ChecklistCategory {
    GENERAL("General"),
    COMMUNICATION("Communication"),
    MODERATION("Moderation"),
    AVAILABILITY("Availability");

    private final String displayName;

    ChecklistCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
