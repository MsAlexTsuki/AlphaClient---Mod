package com.alpharip.staffdashboardexpansion.staffdutymode;

/**
 * Controls one local staff member's duty state.
 *
 * The manager is intentionally in-memory and has no dependency on Alpha Client
 * Core, staff authentication, networking, disk storage, or Supabase.
 */
public final class StaffDutyManager {
    private final StaffDutyMode currentMode;

    public StaffDutyManager(String staffName) {
        currentMode = new StaffDutyMode(staffName);
    }

    public synchronized StaffDutyMode getCurrentMode() {
        return currentMode;
    }

    public synchronized void goOnDuty() {
        currentMode.changeState(StaffDutyState.ON_DUTY);
    }

    public synchronized void goOffDuty() {
        currentMode.changeState(StaffDutyState.OFF_DUTY);
    }

    public synchronized void setBusy() {
        currentMode.changeState(StaffDutyState.BUSY);
    }

    public synchronized void setNote(String note) {
        currentMode.changeNote(note);
    }

    public synchronized boolean isOnDuty() {
        return currentMode.getState().isOnDuty();
    }

    public synchronized boolean canReceiveAssignments() {
        return currentMode.getState().canReceiveAssignments();
    }

    // TODO: connect this local state to a server-authoritative staff duty workflow
    // only when that integration is explicitly scoped and implemented.
}
