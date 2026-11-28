package com.alpharip.staffdashboardexpansion.presence;

public final class StaffPresenceEntry {
    private final String staffName;
    private final String status;
    private final long observedAtMillis;

    public StaffPresenceEntry(String staffName, String status, long observedAtMillis) {
        if (staffName == null || staffName.trim().isEmpty()) {
            throw new IllegalArgumentException("staffName cannot be blank");
        }
        this.staffName = staffName.trim();
        this.status = status == null || status.trim().isEmpty() ? "Unknown" : status.trim();
        this.observedAtMillis = observedAtMillis;
    }

    public String getStaffName() { return staffName; }
    public String getStatus() { return status; }
    public long getObservedAtMillis() { return observedAtMillis; }

    public boolean isStale(long nowMillis, long staleAfterMillis) {
        return nowMillis - observedAtMillis > staleAfterMillis;
    }

    public long getAgeMillis(long nowMillis) {
        return Math.max(0L, nowMillis - observedAtMillis);
    }
}
