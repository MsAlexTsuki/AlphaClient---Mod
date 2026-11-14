package com.alpharip.staffmod.autosave;

/** Local display and placement settings for the AutoSave HUD. */
public final class AutoSaveSettings {
    private boolean enabled = true;
    private boolean showSaveState = true;
    private int recentSaveWindowSeconds = 10;
    private int x = 6;
    private int y = 6;
    private float scale = 1.0F;

    public synchronized boolean isEnabled() { return enabled; }
    public synchronized void setEnabled(boolean enabled) { this.enabled = enabled; }
    public synchronized boolean isShowSaveState() { return showSaveState; }
    public synchronized void setShowSaveState(boolean showSaveState) { this.showSaveState = showSaveState; }
    public synchronized int getRecentSaveWindowSeconds() { return recentSaveWindowSeconds; }
    public synchronized void setRecentSaveWindowSeconds(int seconds) {
        recentSaveWindowSeconds = Math.max(1, Math.min(120, seconds));
    }
    public synchronized int getX() { return x; }
    public synchronized void setX(int x) { this.x = Math.max(0, x); }
    public synchronized int getY() { return y; }
    public synchronized void setY(int y) { this.y = Math.max(0, y); }
    public synchronized float getScale() { return scale; }
    public synchronized void setScale(float scale) {
        if (Float.isNaN(scale) || Float.isInfinite(scale)) return;
        this.scale = Math.max(0.5F, Math.min(2.0F, scale));
    }
    public synchronized void adjustWindow(int amount) { setRecentSaveWindowSeconds(recentSaveWindowSeconds + amount); }
    public synchronized void adjustX(int amount) { setX(x + amount); }
    public synchronized void adjustY(int amount) { setY(y + amount); }
    public synchronized void adjustScale(float amount) { setScale(scale + amount); }
}
