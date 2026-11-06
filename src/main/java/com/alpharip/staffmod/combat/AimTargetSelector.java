package com.alpharip.staffmod.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import java.util.ArrayList;
import java.util.List;

/** Collects visible, living player targets using the existing configured body-area samples. */
public final class AimTargetSelector {
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
        double[] horizontalSamples = {0.10D, 0.50D, 0.90D};
        AimTarget best = null;
        float bestAngle = AimAssistSettings.getMaximumAngleDegrees();
        for (double xFraction : horizontalSamples) {
            for (double zFraction : horizontalSamples) {
                double x = box.minX + widthX * xFraction;
                double z = box.minZ + widthZ * zFraction;
                for (double heightFraction : AimAssistSettings.getTargetArea().getHeightSamples()) {
                    double y = box.minY + height * heightFraction;
                    float angle = AimRotationCalculator.angleTo(mc.thePlayer.posX,
                            mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ,
                            mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, x, y, z);
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
}
