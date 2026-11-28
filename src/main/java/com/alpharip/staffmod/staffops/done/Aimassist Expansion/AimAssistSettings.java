package com.alpharip.staffmod.aimassistexpansion;

/** Local prototype settings. No config file, account service, or remote persistence is used. */
public final class AimAssistSettings {
    private boolean enabled;
    private AimAssistMode mode = AimAssistMode.SMOOTH;
    private AimTargetSelector.Priority priority = AimTargetSelector.Priority.CROSSHAIR;
    private double range = 3.2D;
    private float maximumAngle = 5.0F;
    private float speed = 0.12F;
    private float maximumStep = 1.5F;
    private float predictionTicks = 2.0F;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public AimAssistMode getMode() { return mode; }
    public void setMode(AimAssistMode value) { if (value != null) mode = value; }
    public AimTargetSelector.Priority getPriority() { return priority; }
    public void setPriority(AimTargetSelector.Priority value) { if (value != null) priority = value; }
    public double getRange() { return range; }
    public void setRange(double value) { range = clamp(value, 1.0D, 6.0D); }
    public float getMaximumAngle() { return maximumAngle; }
    public void setMaximumAngle(float value) { maximumAngle = clamp(value, 1.0F, 30.0F); }
    public float getSpeed() { return speed; }
    public void setSpeed(float value) { speed = clamp(value, 0.01F, 1.0F); }
    public float getMaximumStep() { return maximumStep; }
    public void setMaximumStep(float value) { maximumStep = clamp(value, 0.1F, 10.0F); }
    public float getPredictionTicks() { return predictionTicks; }
    public void setPredictionTicks(float value) { predictionTicks = clamp(value, 0.0F, 10.0F); }

    public void reset() {
        enabled = false; mode = AimAssistMode.SMOOTH;
        priority = AimTargetSelector.Priority.CROSSHAIR;
        range = 3.2D; maximumAngle = 5.0F; speed = 0.12F;
        maximumStep = 1.5F; predictionTicks = 2.0F;
    }
    private double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
