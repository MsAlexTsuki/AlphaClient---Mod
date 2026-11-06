package com.alpharip.staffmod.badges.anniversary;

import java.util.Calendar;

/** Supported account-age badge milestones for the Anniversary prototype. */
public enum AnniversaryBadgeTier {
    DAY_ONE("Day 1", Calendar.DAY_OF_MONTH, 1),
    ONE_MONTH("1 Month", Calendar.MONTH, 1),
    THREE_MONTHS("3 Months", Calendar.MONTH, 3),
    SIX_MONTHS("6 Months", Calendar.MONTH, 6),
    ONE_YEAR("1 Year", Calendar.MONTH, 12),
    TWO_YEARS("2 Years", Calendar.MONTH, 24),
    THREE_YEARS("3 Years", Calendar.MONTH, 36),
    FOUR_YEARS("4 Years", Calendar.MONTH, 48),
    FIVE_YEARS("5 Years", Calendar.MONTH, 60);

    private final String displayName;
    private final int calendarField;
    private final int amount;

    AnniversaryBadgeTier(String displayName, int calendarField, int amount) {
        this.displayName = displayName;
        this.calendarField = calendarField;
        this.amount = amount;
    }

    public String getDisplayName() { return displayName; }
    public int getCalendarField() { return calendarField; }
    public int getAmount() { return amount; }
}
