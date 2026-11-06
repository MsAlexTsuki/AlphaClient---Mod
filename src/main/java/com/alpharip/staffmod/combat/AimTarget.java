package com.alpharip.staffmod.combat;

import net.minecraft.entity.player.EntityPlayer;

/** Immutable target sample selected from a player's existing hitbox. */
public final class AimTarget {
    private final EntityPlayer player;
    private final double x, y, z;
    private final double velocityX, velocityY, velocityZ;
    private final double distance;
    private final float angle;

    public AimTarget(EntityPlayer player, double x, double y, double z,
                     double velocityX, double velocityY, double velocityZ,
                     double distance, float angle) {
        this.player = player;
        this.x = x; this.y = y; this.z = z;
        this.velocityX = velocityX; this.velocityY = velocityY; this.velocityZ = velocityZ;
        this.distance = distance;
        this.angle = angle;
    }
    public EntityPlayer getPlayer() { return player; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public double getDistance() { return distance; }
    public float getAngle() { return angle; }
}
