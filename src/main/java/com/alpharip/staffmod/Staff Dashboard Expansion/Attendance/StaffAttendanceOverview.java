package com.alpharip.staffdashboardexpansion.attendance;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local in-memory attendance model. This does not verify server presence or identity.
 * TODO(Supabase): centralize events, validate staff identity, and define retention/access policies.
 */
public class StaffAttendanceOverview {
    private final Map<String, List<StaffAttendanceSession>> sessionsByStaff =
            new LinkedHashMap<String, List<StaffAttendanceSession>>();

    public void clockIn(String staffId, LocalDateTime at) {
        String id = requireText(staffId);
        if (at == null) throw new IllegalArgumentException("at must not be null.");
        List<StaffAttendanceSession> sessions = sessionsByStaff.get(id);
        if (sessions != null && !sessions.isEmpty() && sessions.get(sessions.size() - 1).isOpen()) {
            throw new IllegalStateException("Staff member already has an open attendance session.");
        }
        if (sessions == null) {
            sessions = new ArrayList<StaffAttendanceSession>();
            sessionsByStaff.put(id, sessions);
        }
        sessions.add(new StaffAttendanceSession(at, null));
    }

    public void clockOut(String staffId, LocalDateTime at) {
        String id = requireText(staffId);
        if (at == null) throw new IllegalArgumentException("at must not be null.");
        List<StaffAttendanceSession> sessions = sessionsByStaff.get(id);
        if (sessions == null || sessions.isEmpty() || !sessions.get(sessions.size() - 1).isOpen()) {
            throw new IllegalStateException("No open attendance session exists for this staff member.");
        }
        StaffAttendanceSession open = sessions.get(sessions.size() - 1);
        sessions.set(sessions.size() - 1, new StaffAttendanceSession(open.getCheckIn(), at));
    }

    public List<StaffAttendanceSession> getSessions(String staffId) {
        List<StaffAttendanceSession> sessions = sessionsByStaff.get(requireText(staffId));
        if (sessions == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<StaffAttendanceSession>(sessions));
    }

    public Map<String, List<StaffAttendanceSession>> getAllSessions() {
        Map<String, List<StaffAttendanceSession>> copy =
                new LinkedHashMap<String, List<StaffAttendanceSession>>();
        for (Map.Entry<String, List<StaffAttendanceSession>> entry : sessionsByStaff.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(
                    new ArrayList<StaffAttendanceSession>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    public Duration getTotalDuration(String staffId, LocalDateTime now) {
        if (now == null) throw new IllegalArgumentException("now must not be null.");
        Duration total = Duration.ZERO;
        for (StaffAttendanceSession session : getSessions(staffId)) total = total.plus(session.getDuration(now));
        return total;
    }

    public Duration getDurationForDate(String staffId, LocalDate date, LocalDateTime now) {
        if (date == null || now == null) throw new IllegalArgumentException("date and now are required.");
        Duration total = Duration.ZERO;
        for (StaffAttendanceSession session : getSessions(staffId)) {
            LocalDateTime start = session.getCheckIn().isBefore(date.atStartOfDay())
                    ? date.atStartOfDay() : session.getCheckIn();
            LocalDateTime end = session.isOpen() ? now : session.getCheckOut();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            if (end.isAfter(dayEnd)) end = dayEnd;
            if (end.isAfter(start) && start.isBefore(dayEnd)) total = total.plus(Duration.between(start, end));
        }
        return total;
    }

    public Map<String, Duration> getCurrentTotals(LocalDateTime now) {
        if (now == null) throw new IllegalArgumentException("now must not be null.");
        Map<String, Duration> totals = new LinkedHashMap<String, Duration>();
        for (String id : sessionsByStaff.keySet()) totals.put(id, getTotalDuration(id, now));
        return Collections.unmodifiableMap(totals);
    }

    private static String requireText(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("staffId must not be blank.");
        return value.trim();
    }
}
