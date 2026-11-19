package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;

/** Keeps a pierced arrow moving after the configured number of entity hits. */
public final class PiercingArrowHooks {
    private PiercingArrowHooks() { }

    public static void handleEntityImpact(Object arrowObject) {
        if (!(arrowObject instanceof EntityArrow)) return;
        EntityArrow arrow = (EntityArrow) arrowObject;
        if (arrow.worldObj.isRemote) return;
        NBTTagCompound data = arrow.getEntityData();
        int level = data.getInteger("AlphaPiercing");
        if (level <= 0) {
            arrow.setDead();
            return;
        }
        int remaining = data.hasKey("AlphaPiercingRemaining")
                ? data.getInteger("AlphaPiercingRemaining") : level;
        if (remaining <= 0) {
            arrow.setDead();
            return;
        }
        data.setInteger("AlphaPiercingRemaining", remaining - 1);
        double motionLength = Math.sqrt(arrow.motionX * arrow.motionX
                + arrow.motionY * arrow.motionY + arrow.motionZ * arrow.motionZ);
        if (motionLength < .001D) {
            arrow.setDead();
            return;
        }
        arrow.setPosition(arrow.posX + arrow.motionX / motionLength,
                arrow.posY + arrow.motionY / motionLength,
                arrow.posZ + arrow.motionZ / motionLength);
        arrow.arrowShake = 0;
    }
}