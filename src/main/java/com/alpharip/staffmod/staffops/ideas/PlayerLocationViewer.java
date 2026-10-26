package com.alpharip.staffmod.staffops.ideas;

/**
 * Coordinate snapshot for an entity already visible to the local client.
 * This class does not reveal unloaded or remotely hidden player locations.
 */
public final class PlayerLocationViewer {
    private String playerName = "";
    private double x;
    private double y;
    private double z;
    private long capturedAtMillis;

    public void capture(String playerName, double x, double y, double z, long capturedAtMillis) {
        this.playerName = playerName == null ? "" : playerName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.capturedAtMillis = capturedAtMillis;
    }

    public String getPlayerName() { return playerName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public long getCapturedAtMillis() { return capturedAtMillis; }
}