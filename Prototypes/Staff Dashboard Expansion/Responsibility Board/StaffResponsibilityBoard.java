package com.alpharip.staffmod.staffdashboard.responsibility;

/** Board facade and enable state for responsibility management. */
public class StaffResponsibilityBoard {
    private boolean enabled = true;
    private final StaffResponsibilityManager manager;

    public StaffResponsibilityBoard() { this(new StaffResponsibilityManager()); }
    public StaffResponsibilityBoard(StaffResponsibilityManager manager) {
        if (manager == null) throw new IllegalArgumentException("Manager is required.");
        this.manager = manager;
    }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public StaffResponsibilityManager getManager() { return manager; }
}
