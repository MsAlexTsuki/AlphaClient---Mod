package com.alpharip.staffmod.ChestItemTotal;

/** Local display settings for the chest summary panel. */
public final class ChestItemTotalSettings {
    private boolean enabled = true;
    private int x = 8;
    private int y = 8;

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; }

    public synchronized int getX() { return x; }
    public synchronized void setX(int x) { this.x = Math.max(0, x); }

    public synchronized int getY() { return y; }
    public synchronized void setY(int y) { this.y = Math.max(0, y); }

    public synchronized void adjustX(int amount) { setX(x + amount); }
    public synchronized void adjustY(int amount) { setY(y + amount); }
}
