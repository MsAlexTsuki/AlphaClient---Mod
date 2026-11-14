package com.alpharip.staffmod.AlphaClans;

import java.util.UUID;

public final class ClanMember {
    private final UUID playerId;
    private String playerName;
    private ClanRole role;
    private final long joinedAt;
    private long lastSeenAt;

    public ClanMember(UUID playerId, String playerName, ClanRole role, long joinedAt) {
        if (playerId == null || playerName == null || playerName.trim().isEmpty() || role == null) {
            throw new IllegalArgumentException("Invalid clan member");
        }
        this.playerId = playerId;
        this.playerName = playerName.trim();
        this.role = role;
        this.joinedAt = joinedAt;
        this.lastSeenAt = joinedAt;
    }

    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public ClanRole getRole() { return role; }
    public long getJoinedAt() { return joinedAt; }
    public long getLastSeenAt() { return lastSeenAt; }

    void setPlayerName(String value) {
        if (value != null && !value.trim().isEmpty()) playerName = value.trim();
    }
    void setRole(ClanRole value) {
        if (value == null || value == ClanRole.OWNER) throw new IllegalArgumentException("Use ownership transfer for the owner role");
        role = value;
    }
    void updateLastSeen(long value) { lastSeenAt = Math.max(lastSeenAt, value); }
}
