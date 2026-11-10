package com.alpharip.staffmod.Viewer;

/** Immutable snapshot of connection details visible to this Minecraft client. */
public final class ConnectionSnapshot {
    private final ConnectionState state;
    private final String playerName;
    private final String serverAddress;
    private final String connectionType;
    private final long connectedAtMillis;
    private final long capturedAtMillis;

    public ConnectionSnapshot(ConnectionState state, String playerName,
                              String serverAddress, String connectionType,
                              long connectedAtMillis, long capturedAtMillis) {
        this.state = state == null ? ConnectionState.UNKNOWN : state;
        this.playerName = safe(playerName);
        this.serverAddress = safe(serverAddress);
        this.connectionType = safe(connectionType);
        this.connectedAtMillis = Math.max(0L, connectedAtMillis);
        this.capturedAtMillis = Math.max(0L, capturedAtMillis);
    }

    private static String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Unknown" : value.trim();
    }

    public ConnectionState getState() { return state; }
    public String getPlayerName() { return playerName; }
    public String getServerAddress() { return serverAddress; }
    public String getConnectionType() { return connectionType; }
    public long getConnectedAtMillis() { return connectedAtMillis; }
    public long getCapturedAtMillis() { return capturedAtMillis; }

    public long getSessionDurationMillis() {
        if (state != ConnectionState.CONNECTED || connectedAtMillis <= 0L) return 0L;
        return Math.max(0L, capturedAtMillis - connectedAtMillis);
    }
}
