package com.alpharip.staffdashboardexpansion.attendance;

import java.time.Duration;
import java.time.LocalDateTime;

/** Immutable check-in/check-out record. */
public final class StaffAttendanceSession {
    private final LocalDateTime checkIn;
    private final LocalDateTime checkOut;

    public StaffAttendanceSession(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null) throw new IllegalArgumentException("checkIn must not be null.");
        if (checkOut != null && checkOut.isBefore(checkIn)) {
            throw new IllegalArgumentException("checkOut cannot be before checkIn.");
        }
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public LocalDateTime getCheckIn() { return checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public boolean isOpen() { return checkOut == null; }

    public Duration getDuration(LocalDateTime now) {
        LocalDateTime end = isOpen() ? now : checkOut;
        if (end == null || end.isBefore(checkIn)) return Duration.ZERO;
        return Duration.between(checkIn, end);
    }
}
