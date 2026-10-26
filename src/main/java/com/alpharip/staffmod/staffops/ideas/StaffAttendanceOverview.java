package com.alpharip.staffmod.staffops.ideas;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks staff check-in/check-out sessions and calculates attendance duration.
 *
 * This is local in-memory state, not proof of server presence or identity.
 * TODO(Supabase): store attendance events centrally, validate staff identity,
 * and define retention/access policies before using this as an audit record.
 * Compatible with Java 8.
 */
public class StaffAttendanceOverview {
    public static final class Session {
        private final LocalDateTime checkIn;
        private final LocalDateTime checkOut;

        private Session(LocalDateTime checkIn, LocalDateTime checkOut) {
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        public LocalDateTime getCheckIn() { return checkIn; }
        public LocalDateTime getCheckOut() { return checkOut; }
        public boolean isOpen() { return checkOut == null; }

        public Duration getDuration(LocalDateTime now) {
            LocalDateTime end = checkOut == null ? now : checkOut;
            if (end == null || end.isBefore(checkIn)) {
                return Duration.ZERO;
            }
            return Duration.between(checkIn, end);
        }
    }

    private final Map<String, List<Session>> sessionsByStaff =
            new LinkedHashMap<String, List<Session>>();

    public void clockIn(String staffId, LocalDateTime at) {
        String id = requireText(staffId, "staffId");
        if (at == null) {
            throw new IllegalArgumentException("at must not be null.");
        }
        List<Session> sessions = sessionsByStaff.get(id);
        if (sessions != null && !sessions.isEmpty() && sessions.get(sessions.size() - 1).isOpen()) {
            throw new IllegalStateException("Staff member already has an open attendance session.");
        }
        if (sessions == null) {
            sessions = new ArrayList<Session>();
            sessionsByStaff.put(id, sessions);
        }
        sessions.add(new Session(at, null));
    }

    public void clockOut(String staffId, LocalDateTime at) {
        String id = requireText(staffId, "staffId");
        if (at == null) {
            throw new IllegalArgumentException("at must not be null.");
        }
        List<Session> sessions = sessionsByStaff.get(id);
        if (sessions == null || sessions.isEmpty() || !sessions.get(sessions.size() - 1).isOpen()) {
            throw new IllegalStateException("No open attendance session exists for this staff member.");
        }
        Session open = sessions.get(sessions.size() - 1);
        if (at.isBefore(open.getCheckIn())) {
            throw new IllegalArgumentException("Check-out cannot be before check-in.");
        }
        sessions.set(sessions.size() - 1, new Session(open.getCheckIn(), at));
    }

    public List<Session> getSessions(String staffId) {
        List<Session> sessions = sessionsByStaff.get(requireText(staffId, "staffId"));
        if (sessions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<Session>(sessions));
    }

    public Duration getTotalDuration(String staffId, LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null.");
        }
        Duration total = Duration.ZERO;
        for (Session session : getSessions(staffId)) {
            total = total.plus(session.getDuration(now));
        }
        return total;
    }

    public Map<String, Duration> getCurrentTotals(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null.");
        }
        Map<String, Duration> totals = new LinkedHashMap<String, Duration>();
        for (String staffId : sessionsByStaff.keySet()) {
            totals.put(staffId, getTotalDuration(staffId, now));
        }
        return Collections.unmodifiableMap(totals);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return value.trim();
    }
}
