package com.alpharip.staffmod.Viewer.PlayerDirectionViewer;

/** Immutable direction sample for a player entity currently visible to the client. */
public final class PlayerDirectionSnapshot {
    private final String playerName;
    private final float yaw;
    private final float pitch;
    private final double distance;
    private final long capturedAtMillis;

    public PlayerDirectionSnapshot(String playerName, float yaw, float pitch,
                                   double distance, long capturedAtMillis) {
        this.playerName = playerName == null ? "" : playerName;
        this.yaw = yaw;
        this.pitch = pitch;
        this.distance = Math.max(0.0D, distance);
        this.capturedAtMillis = Math.max(0L, capturedAtMillis);
    }

    public String getPlayerName() { return playerName; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public double getDistance() { return distance; }
    public long getCapturedAtMillis() { return capturedAtMillis; }
}
