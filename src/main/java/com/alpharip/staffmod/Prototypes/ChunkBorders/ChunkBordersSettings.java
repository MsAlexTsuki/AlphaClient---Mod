package com.alpharip.staffmod.chunkborders;

/** Validated local settings. Defaults preserve the original idea scaffold. */
public final class ChunkBordersSettings {
    private boolean enabled = false;
    private ChunkBorderStyle style = ChunkBorderStyle.DEFAULT;
    private int renderRadius = 2;
    private boolean showVerticalPillars = true;
    private boolean showHorizontalGrid = true;
    private boolean highlightCurrentChunk = true;
    private float opacity = 0.75F;

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean value) { enabled = value; }
    public synchronized ChunkBorderStyle getStyle() { return style; }
    public synchronized void setStyle(ChunkBorderStyle value) {
        style = value == null ? ChunkBorderStyle.DEFAULT : value;
    }
    public synchronized int getRenderRadius() { return renderRadius; }
    public synchronized void setRenderRadius(int value) {
        renderRadius = Math.max(1, Math.min(8, value));
    }
    public synchronized boolean isShowVerticalPillars() { return showVerticalPillars; }
    public synchronized void setShowVerticalPillars(boolean value) {
        showVerticalPillars = value;
    }
    public synchronized boolean isShowHorizontalGrid() { return showHorizontalGrid; }
    public synchronized void setShowHorizontalGrid(boolean value) {
        showHorizontalGrid = value;
    }
    public synchronized boolean isHighlightCurrentChunk() { return highlightCurrentChunk; }
    public synchronized void setHighlightCurrentChunk(boolean value) {
        highlightCurrentChunk = value;
    }
    public synchronized float getOpacity() { return opacity; }
    public synchronized void setOpacity(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return;
        opacity = Math.max(0.10F, Math.min(1.0F, value));
    }
}
