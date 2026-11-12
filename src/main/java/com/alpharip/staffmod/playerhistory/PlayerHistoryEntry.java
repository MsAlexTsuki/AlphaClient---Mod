package com.alpharip.staffmod.playerhistory;

import java.util.UUID;

/** Immutable summary of one player observed by this client. */
public final class PlayerHistoryEntry {
    private final UUID playerId;
    private final String playerName;
    private final long firstSeenAt;
    private final long lastSeenAt;
    private final long totalOnlineMillis;
    private final int sessionCount;
    private final boolean online;
    private final long currentSessionStartedAt;

    public PlayerHistoryEntry(UUID playerId, String playerName, long firstSeenAt,
                              long lastSeenAt, long totalOnlineMillis, int sessionCount,
                              boolean online, long currentSessionStartedAt) {
        if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
        this.playerId = playerId;
        this.playerName = playerName == null ? "Unknown" : playerName;
        this.firstSeenAt = firstSeenAt;
        this.lastSeenAt = lastSeenAt;
        this.totalOnlineMillis = Math.max(0L, totalOnlineMillis);
        this.sessionCount = Math.max(0, sessionCount);
        this.online = online;
        this.currentSessionStartedAt = online ? currentSessionStartedAt : 0L;
    }

    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public long getFirstSeenAt() { return firstSeenAt; }
    public long getLastSeenAt() { return lastSeenAt; }
    public long getTotalOnlineMillis() { return totalOnlineMillis; }
    public int getSessionCount() { return sessionCount; }
    public boolean isOnline() { return online; }
    public long getCurrentSessionStartedAt() { return currentSessionStartedAt; }

    public long getTotalOnlineMillisAt(long now) {
        if (!online || currentSessionStartedAt <= 0L) return totalOnlineMillis;
        return totalOnlineMillis + Math.max(0L, now - currentSessionStartedAt);
    }
}
