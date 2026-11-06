package com.alpharip.staffdashboardexpansion.shifts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Standalone in-memory staff shift board.
 * Preserves the original enable/record helper API while adding shift scheduling.
 * TODO: Add approved staff identity, permission checks and shared persistence at integration time.
 */
public class StaffShiftBoard {
    private boolean enabled = true;
    private final List<String> records = new ArrayList<String>();
    private final List<StaffShift> shifts = new ArrayList<StaffShift>();
    private final AtomicLong ids = new AtomicLong();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /** Legacy lightweight record methods retained from the original idea file. */
    public void addRecord(String record) {
        if (record != null && !record.trim().isEmpty()) records.add(record.trim());
    }
    public List<String> getRecords() {
        return Collections.unmodifiableList(new ArrayList<String>(records));
    }
    public void clearRecords() { records.clear(); }

    public StaffShift addShift(String staffName, String title,
                               java.time.LocalDateTime start, java.time.LocalDateTime end) {
        StaffShift shift = new StaffShift(String.valueOf(ids.incrementAndGet()),
                staffName, title, start, end, StaffShiftStatus.SCHEDULED);
        shifts.add(shift);
        return shift;
    }
    public boolean removeShift(String id) {
        for (int i = 0; i < shifts.size(); i++) {
            if (shifts.get(i).getId().equals(id)) { shifts.remove(i); return true; }
        }
        return false;
    }
    public StaffShift getShift(String id) {
        for (StaffShift shift : shifts) if (shift.getId().equals(id)) return shift;
        return null;
    }
    public void updateStatus(String id, StaffShiftStatus status) {
        StaffShift shift = getShift(id);
        if (shift == null) throw new IllegalArgumentException("Unknown shift: " + id);
        shift.setStatus(status);
    }
    public List<StaffShift> getShifts() {
        List<StaffShift> copy = new ArrayList<StaffShift>(shifts);
        Collections.sort(copy, new Comparator<StaffShift>() {
            @Override public int compare(StaffShift a, StaffShift b) {
                return a.getStart().compareTo(b.getStart());
            }
        });
        return Collections.unmodifiableList(copy);
    }
    public List<StaffShift> getShiftsForStaff(String staffName) {
        List<StaffShift> result = new ArrayList<StaffShift>();
        for (StaffShift shift : getShifts())
            if (shift.getStaffName().equalsIgnoreCase(staffName)) result.add(shift);
        return Collections.unmodifiableList(result);
    }
    public List<StaffShift> getConflicts(StaffShift candidate) {
        List<StaffShift> conflicts = new ArrayList<StaffShift>();
        for (StaffShift shift : shifts) if (candidate != null && candidate.overlaps(shift)) conflicts.add(shift);
        return Collections.unmodifiableList(conflicts);
    }
}
