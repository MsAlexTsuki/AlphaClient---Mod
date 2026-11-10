package com.alpharip.staffmod.Viewer;

/** Keeps the latest local connection snapshot and tracks the current session duration. */
public final class ConnectionTracker {
    private ConnectionSnapshot latest;
    private String activeConnectionKey = "";
    private long connectedAtMillis;

    public synchronized ConnectionSnapshot capture(ConnectionState state, String playerName,
                                                    String serverAddress, String connectionType,
                                                    long capturedAtMillis) {
        long now = capturedAtMillis > 0L ? capturedAtMillis : System.currentTimeMillis();
        String key = safe(serverAddress) + "|" + safe(connectionType);

        if (state == ConnectionState.CONNECTED) {
            if (connectedAtMillis <= 0L || !key.equals(activeConnectionKey)) {
                connectedAtMillis = now;
            }
            activeConnectionKey = key;
        } else {
            connectedAtMillis = 0L;
            activeConnectionKey = "";
        }

        latest = new ConnectionSnapshot(state, playerName, serverAddress, connectionType,
                connectedAtMillis, now);
        return latest;
    }

    public synchronized ConnectionSnapshot getLatest() {
        return latest;
    }

    public synchronized void reset() {
        latest = null;
        activeConnectionKey = "";
        connectedAtMillis = 0L;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
