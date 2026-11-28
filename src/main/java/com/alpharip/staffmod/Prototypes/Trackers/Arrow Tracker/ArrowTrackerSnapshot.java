package com.alpharip.staffmod.trackers.arrow;

/** Immutable view of one tracked arrow. */
public final class ArrowTrackerSnapshot {
    private final String id;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double x;
    private final double y;
    private final double z;
    private final long startedAt;
    private final long lastUpdatedAt;

    public ArrowTrackerSnapshot(String id, double startX, double startY, double startZ,
            double x, double y, double z, long startedAt, long lastUpdatedAt) {
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.x = x;
        this.y = y;
        this.z = z;
        this.startedAt = startedAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getId() { return id; }
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public double getStartZ() { return startZ; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public long getStartedAt() { return startedAt; }
    public long getLastUpdatedAt() { return lastUpdatedAt; }
    public long getFlightTimeMillis() { return Math.max(0L, lastUpdatedAt - startedAt); }

    public double getDistanceTravelled() {
        double dx = x - startX;
        double dy = y - startY;
        double dz = z - startZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public double getDistanceTo(double playerX, double playerY, double playerZ) {
        double dx = x - playerX;
        double dy = y - playerY;
        double dz = z - playerZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
