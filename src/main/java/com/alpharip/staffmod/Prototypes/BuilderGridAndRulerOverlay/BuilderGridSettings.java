package com.alpharip.staffmod.buildergridandruleroverlay;

/** Validated overlay settings. Original idea defaults are preserved. */
public final class BuilderGridSettings {
    private boolean gridEnabled = true;
    private boolean rulerEnabled = true;
    private int gridSpacing = 1;
    private int areaRadius = 16;

    public synchronized boolean isGridEnabled() { return gridEnabled; }
    public synchronized void setGridEnabled(boolean value) { gridEnabled = value; }
    public synchronized boolean isRulerEnabled() { return rulerEnabled; }
    public synchronized void setRulerEnabled(boolean value) { rulerEnabled = value; }
    public synchronized int getGridSpacing() { return gridSpacing; }
    public synchronized void setGridSpacing(int value) {
        gridSpacing = Math.max(1, Math.min(64, value));
    }
    public synchronized int getAreaRadius() { return areaRadius; }
    public synchronized void setAreaRadius(int value) {
        areaRadius = Math.max(4, Math.min(64, value));
    }
}
