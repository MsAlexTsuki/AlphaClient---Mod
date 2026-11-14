package com.alpharip.staffmod.animalbreedingcooldowntimer;

/** Local settings for the Animal Breeding Cooldown Timer prototype. */
public final class AnimalBreedingSettings {
    private boolean enabled = true;
    private boolean hudEnabled = true;
    private int intervalSeconds = 60;
    private int x = 8;
    private int y = 8;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isHudEnabled() { return hudEnabled; }
    public void setHudEnabled(boolean hudEnabled) { this.hudEnabled = hudEnabled; }
    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int seconds) {
        intervalSeconds = Math.max(1, Math.min(3600, seconds));
    }
    public void adjustInterval(int delta) { setIntervalSeconds(intervalSeconds + delta); }
    public int getX() { return x; }
    public void setX(int x) { this.x = Math.max(0, Math.min(2000, x)); }
    public int getY() { return y; }
    public void setY(int y) { this.y = Math.max(0, Math.min(2000, y)); }
    public void adjustX(int delta) { setX(x + delta); }
    public void adjustY(int delta) { setY(y + delta); }
}
