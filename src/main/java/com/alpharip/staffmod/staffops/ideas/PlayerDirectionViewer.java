package com.alpharip.staffmod.staffops.ideas;

/**
 * Direction snapshot for an entity already visible to the local client.
 * Yaw and pitch use Minecraft-style degrees supplied by the caller.
 */
public final class PlayerDirectionViewer {
    private String playerName = "";
    private float yaw;
    private float pitch;
    private long capturedAtMillis;

    public void capture(String playerName, float yaw, float pitch, long capturedAtMillis) {
        this.playerName = playerName == null ? "" : playerName;
        this.yaw = yaw;
        this.pitch = pitch;
        this.capturedAtMillis = capturedAtMillis;
    }

    public String getPlayerName() { return playerName; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public long getCapturedAtMillis() { return capturedAtMillis; }
}