package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

import java.time.LocalDate;

/**
 * Year-calendar facade for the standalone prototype.
 * TODO(Supabase): add shared calendar synchronization only in a separately authorized integration task.
 * TODO: Register the screen in the Staff Dashboard and apply the approved staff permissions.
 */
public final class StaffReviewCalendar {
    public static final String NAME = "Staff Review Calendar";
    public static final String CATEGORY = "Management";
    private boolean enabled = true;
    private int displayedYear;
    private final StaffReviewCalendarManager manager;

    public StaffReviewCalendar() {
        this(new StaffReviewCalendarManager(), LocalDate.now().getYear());
    }

    public StaffReviewCalendar(StaffReviewCalendarManager manager, int displayedYear) {
        if (manager == null) throw new IllegalArgumentException("A calendar manager is required.");
        this.manager = manager;
        this.displayedYear = displayedYear;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getDisplayedYear() { return displayedYear; }
    public void setDisplayedYear(int year) {
        if (year < 1 || year > 9999) throw new IllegalArgumentException("Year must be between 1 and 9999.");
        displayedYear = year;
    }
    public void showPreviousYear() { if (displayedYear > 1) displayedYear--; }
    public void showNextYear() { if (displayedYear < 9999) displayedYear++; }
    public void showCurrentYear() { displayedYear = LocalDate.now().getYear(); }
    public StaffReviewCalendarManager getManager() { return manager; }
}
