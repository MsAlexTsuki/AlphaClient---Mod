package com.alpharip.staffmod.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import java.util.ArrayList;
import java.util.List;

/** Collects visible, living player targets using the configured body-area samples. */
public final class AimTargetSelector {
    private static final double ACQUISITION_MARGIN_BLOCKS = 0.5D;
    private static final double[] ACQUISITION_SAMPLES = {0.0D, 0.25D, 0.50D, 0.75D, 1.0D};

    public List<AimTarget> collect(Minecraft mc) {
        List<AimTarget> targets = new ArrayList<AimTarget>();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return targets;
        for (Object object : mc.theWorld.playerEntities) {
            if (!(object instanceof EntityPlayer)) continue;
            EntityPlayer target = (EntityPlayer) object;
            if (target == mc.thePlayer || target.isDead || !target.isEntityAlive()
                    || target.isInvisible()
                    || mc.thePlayer.getDistanceToEntity(target) > AimAssistSettings.getMaximumRange()
                    || !mc.thePlayer.canEntityBeSeen(target)) continue;
            AimTarget sample = findBestSample(mc, target);
            if (sample != null) targets.add(sample);
        }
        return targets;
    }

    private AimTarget findBestSample(Minecraft mc, EntityPlayer target) {
        AxisAlignedBB box = target.getEntityBoundingBox();
        double widthX = box.maxX - box.minX;
        double widthZ = box.maxZ - box.minZ;
        double height = box.maxY - box.minY;
        double eyeX = mc.thePlayer.posX;
        double eyeY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        double eyeZ = mc.thePlayer.posZ;
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;

        if (!isWithinAcquisitionArea(box, eyeX, eyeY, eyeZ, yaw, pitch)) return null;

        double[] horizontalSamples = {0.10D, 0.50D, 0.90D};
        AimTarget best = null;
        float bestAngle = Float.MAX_VALUE;
        for (double xFraction : horizontalSamples) {
            for (double zFraction : horizontalSamples) {
                double x = box.minX + widthX * xFraction;
                double z = box.minZ + widthZ * zFraction;
                for (double heightFraction : AimAssistSettings.getTargetArea().getHeightSamples()) {
                    double y = box.minY + height * heightFraction;
                    float angle = AimRotationCalculator.angleTo(eyeX, eyeY, eyeZ,
                            yaw, pitch, x, y, z);
                    if (angle < bestAngle) {
                        bestAngle = angle;
                        best = new AimTarget(target, x, y, z,
                                target.posX - target.prevPosX,
                                target.posY - target.prevPosY,
                                target.posZ - target.prevPosZ,
                                mc.thePlayer.getDistanceToEntity(target), angle);
                    }
                }
            }
        }
        return best;
    }

    /**
     * Allows acquisition when the current view is within 0.5 blocks of the
     * physical hitbox. The selected correction point remains on the real hitbox.
     */
    private boolean isWithinAcquisitionArea(AxisAlignedBB box,
                                            double eyeX, double eyeY, double eyeZ,
                                            float yaw, float pitch) {
        AxisAlignedBB expanded = box.expand(ACQUISITION_MARGIN_BLOCKS,
                ACQUISITION_MARGIN_BLOCKS, ACQUISITION_MARGIN_BLOCKS);
        float bestAngle = AimAssistSettings.getMaximumAngleDegrees();
        double widthX = expanded.maxX - expanded.minX;
        double widthY = expanded.maxY - expanded.minY;
        double widthZ = expanded.maxZ - expanded.minZ;

        for (double xFraction : ACQUISITION_SAMPLES) {
            double x = expanded.minX + widthX * xFraction;
            for (double yFraction : ACQUISITION_SAMPLES) {
                double y = expanded.minY + widthY * yFraction;
                for (double zFraction : ACQUISITION_SAMPLES) {
                    double z = expanded.minZ + widthZ * zFraction;
                    float angle = AimRotationCalculator.angleTo(eyeX, eyeY, eyeZ,
                            yaw, pitch, x, y, z);
                    if (angle < bestAngle) bestAngle = angle;
                    if (bestAngle == 0.0F) return true;
                }
            }
        }
        return bestAngle < AimAssistSettings.getMaximumAngleDegrees();
    }
}
