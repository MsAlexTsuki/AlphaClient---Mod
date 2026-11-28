package com.alpharip.staffmod.fishingbitealarm;

import net.minecraft.entity.projectile.EntityFishHook;

/** Immutable client-side snapshot of the local player's active fishing bobber. */
public final class FishingHookSample {
    private final int entityId;
    private final double y;
    private final double motionY;
    private final boolean inWater;

    public FishingHookSample(int entityId, double y, double motionY, boolean inWater) {
        this.entityId = entityId;
        this.y = y;
        this.motionY = motionY;
        this.inWater = inWater;
    }

    public static FishingHookSample from(EntityFishHook hook) {
        if (hook == null) return null;
        return new FishingHookSample(
                hook.getEntityId(), hook.posY, hook.motionY, hook.isInWater());
    }

    public int getEntityId() { return entityId; }
    public double getY() { return y; }
    public double getMotionY() { return motionY; }
    public boolean isInWater() { return inWater; }
}
