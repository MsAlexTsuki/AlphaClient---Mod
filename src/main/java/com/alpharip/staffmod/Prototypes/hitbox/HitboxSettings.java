package com.alpharip.staffmod.hitbox;

/**
 * Runtime settings for the Hitbox prototype.
 *
 * Visibility restrictions are intentional: hitboxes never render through
 * walls and invisible players are never included.
 */
public final class HitboxSettings {
    private boolean enabled;
    private double range = 32.0D;
    private float lineWidth = 1.4F;
    private float red = 1.0F;
    private float green = 0.78F;
    private float blue = 0.20F;
    private float alpha = 0.95F;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public double getRange() { return range; }
    public void setRange(double range) {
        if (!Double.isNaN(range) && !Double.isInfinite(range)) {
            this.range = clamp(range, 1.0D, 128.0D);
        }
    }

    public float getLineWidth() { return lineWidth; }
    public void setLineWidth(float lineWidth) {
        if (!Float.isNaN(lineWidth) && !Float.isInfinite(lineWidth)) {
            this.lineWidth = clamp(lineWidth, 0.5F, 5.0F);
        }
    }

    public float getRed() { return red; }
    public float getGreen() { return green; }
    public float getBlue() { return blue; }
    public float getAlpha() { return alpha; }

    public void setColor(float red, float green, float blue, float alpha) {
        this.red = clamp(red, 0.0F, 1.0F);
        this.green = clamp(green, 0.0F, 1.0F);
        this.blue = clamp(blue, 0.0F, 1.0F);
        this.alpha = clamp(alpha, 0.05F, 1.0F);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
