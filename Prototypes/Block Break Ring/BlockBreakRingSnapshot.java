package com.alpharip.staffmod.blockbreakring;

/** Immutable view of the module state for other prototype components. */
public final class BlockBreakRingSnapshot {
    private final boolean enabled, running, showBackground, smoothAnimation;
    private final float progress, radius, opacity;
    private final int lineWidth, colorIndex;

    public BlockBreakRingSnapshot(boolean enabled, boolean running, float progress,
            int lineWidth, float radius, int colorIndex, float opacity,
            boolean showBackground, boolean smoothAnimation) {
        this.enabled = enabled;
        this.running = running;
        this.progress = progress;
        this.lineWidth = lineWidth;
        this.radius = radius;
        this.colorIndex = colorIndex;
        this.opacity = opacity;
        this.showBackground = showBackground;
        this.smoothAnimation = smoothAnimation;
    }
    public boolean isEnabled() { return enabled; }
    public boolean isRunning() { return running; }
    public float getProgress() { return progress; }
    public int getLineWidth() { return lineWidth; }
    public float getRadius() { return radius; }
    public int getColorIndex() { return colorIndex; }
    public float getOpacity() { return opacity; }
    public boolean isShowBackground() { return showBackground; }
    public boolean isSmoothAnimation() { return smoothAnimation; }
}
