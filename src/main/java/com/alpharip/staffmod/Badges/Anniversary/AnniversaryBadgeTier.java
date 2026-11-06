package com.alpharip.staffmod.badges.anniversary;

/** Supported account-age badge milestones for the Anniversary prototype. */
public enum AnniversaryBadgeTier {
    THREE_MONTHS("3 Months", 3),
    SIX_MONTHS("6 Months", 6),
    ONE_YEAR("1 Year", 12),
    TWO_YEARS("2 Years", 24),
    THREE_YEARS("3 Years", 36),
    FOUR_YEARS("4 Years", 48),
    FIVE_YEARS("5 Years", 60);

    private final String displayName;
    private final int monthsRequired;

    AnniversaryBadgeTier(String displayName, int monthsRequired) {
        this.displayName = displayName;
        this.monthsRequired = monthsRequired;
    }

    public String getDisplayName() { return displayName; }
    public int getMonthsRequired() { return monthsRequired; }
}
