package com.alpharip.staffdashboardexpansion.shifts;

import java.time.Duration;
import java.time.LocalDateTime;

/** Immutable shift details with a mutable lifecycle status. */
public final class StaffShift {
    private final String id;
    private final String staffName;
    private final String title;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private StaffShiftStatus status;

    public StaffShift(String id, String staffName, String title,
                      LocalDateTime start, LocalDateTime end, StaffShiftStatus status) {
        this.id = require(id, "id");
        this.staffName = require(staffName, "staffName");
        this.title = require(title, "title");
        if (start == null || end == null || !end.isAfter(start))
            throw new IllegalArgumentException("Shift end must be after shift start.");
        this.start = start;
        this.end = end;
        this.status = status == null ? StaffShiftStatus.SCHEDULED : status;
    }
    public String getId() { return id; }
    public String getStaffName() { return staffName; }
    public String getTitle() { return title; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public StaffShiftStatus getStatus() { return status; }
    public void setStatus(StaffShiftStatus status) {
        if (status == null) throw new IllegalArgumentException("status must not be null.");
        this.status = status;
    }
    public boolean overlaps(StaffShift other) {
        return other != null && staffName.equalsIgnoreCase(other.staffName)
                && start.isBefore(other.end) && other.start.isBefore(end)
                && status != StaffShiftStatus.CANCELLED
                && other.status != StaffShiftStatus.CANCELLED;
    }
    public Duration getDuration() { return Duration.between(start, end); }
    private static String require(String value, String field) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(field + " must not be blank.");
        return value.trim();
    }
}
