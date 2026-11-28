package com.alpharip.staffmod.playerhistory;

import java.util.UUID;

/** One locally observed presence transition. */
public final class PlayerHistoryEvent {
    public enum Type {
        FIRST_OBSERVED,
        JOINED,
        LEFT
    }

    private final UUID playerId;
    private final String playerName;
    private final Type type;
    private final long timestamp;
    private final String connectionContext;

    public PlayerHistoryEvent(UUID playerId, String playerName, Type type,
                              long timestamp, String connectionContext) {
        if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        this.playerId = playerId;
        this.playerName = playerName == null ? "Unknown" : playerName;
        this.type = type;
        this.timestamp = timestamp;
        this.connectionContext = connectionContext == null ? "Unknown" : connectionContext;
    }

    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public Type getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public String getConnectionContext() { return connectionContext; }
}
