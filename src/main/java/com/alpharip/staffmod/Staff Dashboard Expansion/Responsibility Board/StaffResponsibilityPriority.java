package com.alpharip.staffmod.staffdashboard.responsibility;

public enum StaffResponsibilityPriority {
    LOW("Low"), NORMAL("Normal"), HIGH("High"), CRITICAL("Critical");
    private final String displayName;
    StaffResponsibilityPriority(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
