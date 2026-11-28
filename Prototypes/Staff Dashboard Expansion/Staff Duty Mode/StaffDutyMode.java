package com.alpharip.staffdashboardexpansion.staffdutymode;

/**
 * Local staff duty state for the standalone Staff Duty Mode prototype.
 *
 * This model is deliberately client-local. It does not grant staff permissions
 * or claim that a server has accepted the duty state.
 */
public final class StaffDutyMode {
    private final String staffName;
    private StaffDutyState state;
    private long stateSinceMillis;
    private String note;

    public StaffDutyMode(String staffName) {
        this.staffName = requireText(staffName, "staffName");
        this.state = StaffDutyState.OFF_DUTY;
        this.stateSinceMillis = System.currentTimeMillis();
        this.note = "";
    }

    public String getStaffName() {
        return staffName;
    }

    public synchronized StaffDutyState getState() {
        return state;
    }

    public synchronized long getStateSinceMillis() {
        return stateSinceMillis;
    }

    public synchronized String getNote() {
        return note;
    }

    public synchronized long getStateDurationMillis() {
        return Math.max(0L, System.currentTimeMillis() - stateSinceMillis);
    }

    synchronized void changeState(StaffDutyState nextState) {
        if (nextState == null) {
            throw new IllegalArgumentException("nextState must not be null.");
        }
        if (state != nextState) {
            state = nextState;
            stateSinceMillis = System.currentTimeMillis();
        }
    }

    synchronized void changeNote(String nextNote) {
        note = nextNote == null ? "" : nextNote.trim();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }

    // TODO(Supabase): add optional server-backed duty history only after a separate integration design.
    // TODO(Server): a production duty state must be validated by the server, not this local model.
}
