package com.alpharip.staffmod.staffops.ideas;

/**
 * Local summary model for safety indicators shown in a future client dashboard.
 * It is not a live monitoring system and does not receive staff/server events.
 * TODO(Supabase): define trusted safety event sources and read permissions if
 * shared dashboard data is required.
 */
public final class StaffSafetyDashboard {
    public static final String FEATURE_NAME = "Staff Safety Dashboard";

    private int activeWarnings;
    private int pendingReviews;
    private int unresolvedItems;

    public int getActiveWarnings() { return activeWarnings; }
    public int getPendingReviews() { return pendingReviews; }
    public int getUnresolvedItems() { return unresolvedItems; }

    public void setCounts(int activeWarnings, int pendingReviews, int unresolvedItems) {
        if (activeWarnings < 0 || pendingReviews < 0 || unresolvedItems < 0) {
            throw new IllegalArgumentException("Dashboard counts cannot be negative");
        }
        this.activeWarnings = activeWarnings;
        this.pendingReviews = pendingReviews;
        this.unresolvedItems = unresolvedItems;
    }
}
