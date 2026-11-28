package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

import java.time.LocalDate;

/** Derived local reminder for a scheduled review. */
public final class StaffReviewReminder {
    private final int reviewId;
    private final LocalDate reminderDate;
    private final String message;
    private final boolean overdue;

    public StaffReviewReminder(int reviewId, LocalDate reminderDate, String message, boolean overdue) {
        if (reminderDate == null) throw new IllegalArgumentException("Reminder date is required.");
        this.reviewId = reviewId;
        this.reminderDate = reminderDate;
        this.message = message == null ? "" : message;
        this.overdue = overdue;
    }

    public int getReviewId() { return reviewId; }
    public LocalDate getReminderDate() { return reminderDate; }
    public String getMessage() { return message; }
    public boolean isOverdue() { return overdue; }
}
