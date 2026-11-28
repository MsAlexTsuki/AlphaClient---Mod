package com.alpharip.staffmod.ArrowCounter;

/** Local HUD settings for ArrowCounter. */
public final class ArrowCounterSettings {
    private boolean enabled = true;
    private int x = 6;
    private int y = 170;
    private float scale = 1.0F;

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; }

    public synchronized int getX() { return x; }
    public synchronized void setX(int x) { this.x = Math.max(0, x); }

    public synchronized int getY() { return y; }
    public synchronized void setY(int y) { this.y = Math.max(0, y); }

    public synchronized float getScale() { return scale; }
    public synchronized void setScale(float scale) {
        this.scale = Math.max(0.5F, Math.min(2.0F, scale));
    }

    public synchronized void adjustX(int amount) { setX(x + amount); }
    public synchronized void adjustY(int amount) { setY(y + amount); }
    public synchronized void adjustScale(float amount) { setScale(scale + amount); }
}
