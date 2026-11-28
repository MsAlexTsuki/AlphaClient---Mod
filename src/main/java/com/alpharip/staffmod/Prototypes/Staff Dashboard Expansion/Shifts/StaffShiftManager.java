package com.alpharip.staffdashboardexpansion.shifts;

import java.time.LocalDateTime;
import java.util.List;

/** Application-facing operations and conflict checks for the shift board. */
public final class StaffShiftManager {
    private final StaffShiftBoard board;
    public StaffShiftManager() { this(new StaffShiftBoard()); }
    public StaffShiftManager(StaffShiftBoard board) {
        if (board == null) throw new IllegalArgumentException("board must not be null.");
        this.board = board;
    }
    public StaffShift schedule(String staff, String title, LocalDateTime start, LocalDateTime end) {
        StaffShift candidate = new StaffShift("preview", staff, title, start, end, StaffShiftStatus.SCHEDULED);
        List<StaffShift> conflicts = board.getConflicts(candidate);
        if (!conflicts.isEmpty()) throw new IllegalStateException(
                "This staff member already has an overlapping shift (" + conflicts.get(0).getTitle() + ").");
        return board.addShift(staff, title, start, end);
    }
    public void cycleStatus(String id) {
        StaffShift shift = board.getShift(id);
        if (shift == null) throw new IllegalArgumentException("Unknown shift: " + id);
        board.updateStatus(id, shift.getStatus().next());
    }
    public StaffShiftBoard getBoard() { return board; }
}
