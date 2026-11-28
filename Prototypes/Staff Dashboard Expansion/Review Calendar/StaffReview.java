package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

import java.time.LocalDate;
import java.time.LocalTime;

/** One dated staff review stored by the local calendar prototype. */
public final class StaffReview {
    private final int id;
    private final long createdAt;
    private LocalDate date;
    private LocalTime time;
    private String title;
    private String staffName;
    private String notes;
    private StaffReviewStatus status;
    private int reminderDaysBefore;
    private boolean reminderAcknowledged;

    public StaffReview(int id, LocalDate date, LocalTime time, String title,
                       String staffName, String notes, int reminderDaysBefore) {
        this.id = id;
        this.createdAt = System.currentTimeMillis();
        updateDetails(date, time, title, staffName, notes, reminderDaysBefore);
        this.status = StaffReviewStatus.SCHEDULED;
    }

    public void updateDetails(LocalDate date, LocalTime time, String title,
                             String staffName, String notes, int reminderDaysBefore) {
        if (date == null) throw new IllegalArgumentException("A review date is required.");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("A review title is required.");
        if (staffName == null || staffName.trim().isEmpty()) throw new IllegalArgumentException("A staff member is required.");
        if (reminderDaysBefore < 0 || reminderDaysBefore > 365) {
            throw new IllegalArgumentException("Reminder days must be between 0 and 365.");
        }
        this.date = date;
        this.time = time;
        this.title = title.trim();
        this.staffName = staffName.trim();
        this.notes = notes == null ? "" : notes.trim();
        this.reminderDaysBefore = reminderDaysBefore;
        this.reminderAcknowledged = false;
    }

    public int getId() { return id; }
    public long getCreatedAt() { return createdAt; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getTitle() { return title; }
    public String getStaffName() { return staffName; }
    public String getNotes() { return notes; }
    public StaffReviewStatus getStatus() { return status; }
    public int getReminderDaysBefore() { return reminderDaysBefore; }
    public boolean isReminderAcknowledged() { return reminderAcknowledged; }
    public void setStatus(StaffReviewStatus status) {
        if (status == null) throw new IllegalArgumentException("A review status is required.");
        this.status = status;
    }
    public void acknowledgeReminder() { reminderAcknowledged = true; }
    public void resetReminder() { reminderAcknowledged = false; }
    public boolean isOverdue(LocalDate today) {
        return status == StaffReviewStatus.SCHEDULED && date.isBefore(today);
    }
    public LocalDate getReminderDate() { return date.minusDays(reminderDaysBefore); }
}
