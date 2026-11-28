package com.alpharip.staffdashboardexpansion.attendance;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Small application-facing facade for attendance actions and summaries. */
public final class StaffAttendanceManager {
    private final StaffAttendanceOverview overview;

    public StaffAttendanceManager() { this(new StaffAttendanceOverview()); }
    public StaffAttendanceManager(StaffAttendanceOverview overview) {
        if (overview == null) throw new IllegalArgumentException("overview must not be null.");
        this.overview = overview;
    }

    public void checkIn(String staffName) { overview.clockIn(staffName, LocalDateTime.now()); }
    public void checkOut(String staffName) { overview.clockOut(staffName, LocalDateTime.now()); }
    public boolean isCheckedIn(String staffName) {
        List<StaffAttendanceSession> sessions = overview.getSessions(staffName);
        return !sessions.isEmpty() && sessions.get(sessions.size() - 1).isOpen();
    }
    public Duration getTotal(String staffName) { return overview.getTotalDuration(staffName, LocalDateTime.now()); }
    public Duration getForDate(String staffName, LocalDate date) {
        return overview.getDurationForDate(staffName, date, LocalDateTime.now());
    }
    public Map<String, List<StaffAttendanceSession>> getAllSessions() { return overview.getAllSessions(); }
    public List<String> getStaffNames() {
        return Collections.unmodifiableList(new ArrayList<String>(overview.getAllSessions().keySet()));
    }
    public StaffAttendanceOverview getOverview() { return overview; }
}
