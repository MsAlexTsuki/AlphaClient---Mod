package com.alpharip.staffmod.staffops.ideas;

/**
 * Velocity snapshot for an entity whose motion is available to the local client.
 * It is observational only and does not modify entity motion.
 */
public final class PlayerVelocityViewer {
    private String playerName = "";
    private double motionX;
    private double motionY;
    private double motionZ;
    private long capturedAtMillis;

    public void capture(String playerName, double motionX, double motionY,
                        double motionZ, long capturedAtMillis) {
        this.playerName = playerName == null ? "" : playerName;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.capturedAtMillis = capturedAtMillis;
    }

    public String getPlayerName() { return playerName; }
    public double getMotionX() { return motionX; }
    public double getMotionY() { return motionY; }
    public double getMotionZ() { return motionZ; }
    public long getCapturedAtMillis() { return capturedAtMillis; }
}