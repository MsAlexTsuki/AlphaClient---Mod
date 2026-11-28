package com.alpharip.staffmod.blockbreakring;

/** Mutable, local-only settings for the progress ring. */
public final class BlockBreakRingConfig {
    private boolean enabled = true;
    private boolean showBackground = true;
    private boolean smoothAnimation = true;
    private int lineWidth = 3;
    private float radius = 0.34F;
    private float opacity = 0.95F;
    private int colorIndex = 0;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public boolean isShowBackground() { return showBackground; }
    public void setShowBackground(boolean value) { showBackground = value; }
    public boolean isSmoothAnimation() { return smoothAnimation; }
    public void setSmoothAnimation(boolean value) { smoothAnimation = value; }
    public int getLineWidth() { return lineWidth; }
    public void setLineWidth(int value) { lineWidth = clamp(value, 1, 12); }
    public float getRadius() { return radius; }
    public void setRadius(float value) { radius = clamp(value, 0.18F, 0.48F); }
    public float getOpacity() { return opacity; }
    public void setOpacity(float value) { opacity = clamp(value, 0.10F, 1.0F); }
    public int getColorIndex() { return colorIndex; }
    public void setColorIndex(int value) { colorIndex = Math.floorMod(value, 5); }

    public int getColorRgb() {
        switch (colorIndex) {
            case 1: return 0xFF5555;
            case 2: return 0x55FF88;
            case 3: return 0x55AAFF;
            case 4: return 0xFFFFFF;
            default: return 0xFFC83D;
        }
    }

    public String getColorName() {
        switch (colorIndex) {
            case 1: return "Red";
            case 2: return "Green";
            case 3: return "Blue";
            case 4: return "White";
            default: return "Alpha Gold";
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    private static float clamp(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }
}
