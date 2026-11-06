package com.alpharip.staffmod.aimassistexpansion;

/** Immutable target position used by the expansion's targeting/calculation layer. */
public final class AimTarget {
    private final Object entity;
    private final double x, y, z;
    private final double velocityX, velocityY, velocityZ;
    private final double distance;

    public AimTarget(Object entity, double x, double y, double z,
                     double velocityX, double velocityY, double velocityZ, double distance) {
        this.entity = entity;
        this.x = x; this.y = y; this.z = z;
        this.velocityX = velocityX; this.velocityY = velocityY; this.velocityZ = velocityZ;
        this.distance = distance;
    }
    public Object getEntity() { return entity; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public double getDistance() { return distance; }
}
