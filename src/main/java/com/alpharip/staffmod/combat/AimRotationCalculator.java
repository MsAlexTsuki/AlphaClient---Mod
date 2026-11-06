package com.alpharip.staffmod.combat;

import net.minecraft.util.MathHelper;

/** Calculates normalized yaw/pitch deltas without mutating player rotation. */
public final class AimRotationCalculator {
    public float[] calculate(double originX, double originY, double originZ,
                             float currentYaw, float currentPitch,
                             double targetX, double targetY, double targetZ) {
        double dx = targetX - originX, dy = targetY - originY, dz = targetZ - originZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, horizontal) * 180.0D / Math.PI));
        return new float[] {
            MathHelper.wrapAngleTo180_float(yaw - currentYaw),
            MathHelper.wrapAngleTo180_float(pitch - currentPitch)
        };
    }

    public static float angleTo(double originX, double originY, double originZ,
                                float currentYaw, float currentPitch,
                                double targetX, double targetY, double targetZ) {
        double dx = targetX - originX, dy = targetY - originY, dz = targetZ - originZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, horizontal) * 180.0D / Math.PI));
        float yawDelta = MathHelper.wrapAngleTo180_float(yaw - currentYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float(pitch - currentPitch);
        return (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
    }
}
