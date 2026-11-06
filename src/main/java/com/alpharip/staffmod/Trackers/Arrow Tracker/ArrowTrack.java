package com.alpharip.staffmod.trackers.arrow;

/** Mutable internal state for one tracked arrow. */
public final class ArrowTrack {
    private final String id;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final long startedAt;
    private double x;
    private double y;
    private double z;
    private long lastUpdatedAt;

    public ArrowTrack(String id, double x, double y, double z, long timestamp) {
        if (id == null || id.trim().isEmpty() || timestamp < 0L) {
            throw new IllegalArgumentException("Invalid arrow identity or timestamp");
        }
        this.id = id;
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.x = x;
        this.y = y;
        this.z = z;
        this.startedAt = timestamp;
        this.lastUpdatedAt = timestamp;
    }

    public void updatePosition(double x, double y, double z, long timestamp) {
        if (timestamp < lastUpdatedAt) {
            throw new IllegalArgumentException("timestamp cannot move backwards");
        }
        this.x = x;
        this.y = y;
        this.z = z;
        this.lastUpdatedAt = timestamp;
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

    public ArrowTrackerSnapshot snapshot() {
        return new ArrowTrackerSnapshot(id, startX, startY, startZ,
                x, y, z, startedAt, lastUpdatedAt);
    }
}
