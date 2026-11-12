package com.alpharip.staffmod.AirHud;

/** Local settings for Air HUD. */
public final class AirHudSettings {
    public enum Position { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private boolean enabled = true;
    private boolean showLabel = true;
    private Position position = Position.BOTTOM_LEFT;
    private int colorIndex;

    private static final int[] COLORS = {
        0xFFFFFFFF, 0xFF55FFFF, 0xFFFFFF55, 0xFF55FF55, 0xFFFF5555
    };

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean value) { enabled = value; }
    public synchronized boolean isShowLabel() { return showLabel; }
    public synchronized void setShowLabel(boolean value) { showLabel = value; }
    public synchronized Position getPosition() { return position; }
    public synchronized void cyclePosition() {
        Position[] values = Position.values();
        position = values[(position.ordinal() + 1) % values.length];
    }
    public synchronized int getColor() { return COLORS[colorIndex]; }
    public synchronized int getColorIndex() { return colorIndex; }
    public synchronized void setColorIndex(int value) {
        colorIndex = Math.max(0, Math.min(COLORS.length - 1, value));
    }
}
