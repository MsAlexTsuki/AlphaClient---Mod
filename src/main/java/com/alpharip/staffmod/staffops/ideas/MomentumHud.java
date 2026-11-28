package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for a movement momentum HUD.
 * Player motion sampling and HUD rendering are not connected yet.
 */
public final class MomentumHud {
    public static final String FEATURE_NAME = "Momentum HUD";

    private boolean enabled = false;
    private boolean showSpeed = true;
    private boolean showAcceleration = true;
    private boolean showDirection = false;
    private double previousSpeed;
    private double currentSpeed;
    private double acceleration;
    private long previousSampleMillis;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public boolean isShowSpeed() { return showSpeed; }
    public void setShowSpeed(boolean value) { showSpeed = value; }
    public boolean isShowAcceleration() { return showAcceleration; }
    public void setShowAcceleration(boolean value) { showAcceleration = value; }
    public boolean isShowDirection() { return showDirection; }
    public void setShowDirection(boolean value) { showDirection = value; }

    /** Updates speed in blocks per second using a horizontal motion sample. */
    public void update(double motionX, double motionZ, long nowMillis) {
        if (Double.isNaN(motionX) || Double.isInfinite(motionX)
                || Double.isNaN(motionZ) || Double.isInfinite(motionZ)) return;
        double speed = Math.sqrt(motionX * motionX + motionZ * motionZ) * 20.0D;
        if (previousSampleMillis > 0L && nowMillis > previousSampleMillis) {
            double seconds = (nowMillis - previousSampleMillis) / 1000.0D;
            acceleration = (speed - previousSpeed) / seconds;
        } else {
            acceleration = 0.0D;
        }
        previousSpeed = speed;
        currentSpeed = speed;
        previousSampleMillis = Math.max(0L, nowMillis);
    }

    public double getCurrentSpeed() { return currentSpeed; }
    public double getAcceleration() { return acceleration; }

    public void reset() {
        previousSpeed = 0.0D;
        currentSpeed = 0.0D;
        acceleration = 0.0D;
        previousSampleMillis = 0L;
    }
}
