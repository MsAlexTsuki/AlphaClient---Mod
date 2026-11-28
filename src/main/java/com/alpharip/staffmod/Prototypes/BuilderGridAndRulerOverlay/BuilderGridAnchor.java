package com.alpharip.staffmod.buildergridandruleroverlay;

/** World-space origin for the guide area and ruler measurements. */
public final class BuilderGridAnchor {
    private int x;
    private int y;
    private int z;
    private boolean initialized;

    public synchronized int getX() { return x; }
    public synchronized int getY() { return y; }
    public synchronized int getZ() { return z; }
    public synchronized boolean isInitialized() { return initialized; }

    public synchronized void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        initialized = true;
    }
}
