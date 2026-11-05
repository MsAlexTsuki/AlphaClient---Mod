package com.alpharip.staffmod.AccessReviewReminder;

/** One staff member's access-review schedule. */
public final class AccessReviewEntry {
    public enum ReviewStatus {
        DUE, UPCOMING, CURRENT
    }

    private final String id;
    private String staffName;
    private String rank;
    private long lastReviewedAt;
    private int intervalDays;

    public AccessReviewEntry(String id, String staffName, String rank,
                             long lastReviewedAt, int intervalDays) {
        this.id = id;
        this.staffName = staffName;
        this.rank = rank == null ? "" : rank;
        this.lastReviewedAt = lastReviewedAt;
        this.intervalDays = intervalDays;
    }

    public String getId() { return id; }
    public String getStaffName() { return staffName; }
    public String getRank() { return rank; }
    public long getLastReviewedAt() { return lastReviewedAt; }
    public int getIntervalDays() { return intervalDays; }

    public long getNextReviewAt() {
        if (lastReviewedAt <= 0L) return -1L;
        long intervalMillis = intervalDays * 86400000L;
        if (Long.MAX_VALUE - lastReviewedAt < intervalMillis) return Long.MAX_VALUE;
        return lastReviewedAt + intervalMillis;
    }

    public ReviewStatus getStatus(long now) {
        long dueAt = getNextReviewAt();
        if (lastReviewedAt <= 0L || dueAt < 0L || now >= dueAt) {
            return ReviewStatus.DUE;
        }
        return dueAt - now <= 7L * 86400000L
                ? ReviewStatus.UPCOMING : ReviewStatus.CURRENT;
    }

    void markReviewed(long timestamp) {
        lastReviewedAt = timestamp;
    }

    void update(String name, String newRank, int days) {
        staffName = name;
        rank = newRank == null ? "" : newRank;
        intervalDays = days;
    }
}