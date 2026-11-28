package com.alpharip.staffmod.staffops.ideas;

import java.time.Instant;

/**
 * Foundation for displaying a live launcher user count.
 * TODO(Supabase): publish/read privacy-safe presence aggregates.
 * Presence heartbeats, stale-session cleanup, UI integration, and tests remain to be implemented.
 */
public class LiveLauncherUserCount {
    private int activeUsers;
    private Instant lastUpdated;
    private boolean available;

    public int getActiveUsers() { return activeUsers; }
    public void updateCount(int value, Instant updatedAt) {
        activeUsers = Math.max(0, value);
        lastUpdated = updatedAt;
        available = updatedAt != null;
    }
    public Instant getLastUpdated() { return lastUpdated; }
    public boolean isAvailable() { return available; }
}