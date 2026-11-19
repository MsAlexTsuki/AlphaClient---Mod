package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;

/** Keeps a pierced arrow moving after the configured number of entity hits. */
public final class PiercingArrowHooks {
    private PiercingArrowHooks() { }

    public static void handleEntityImpact(Object arrowObject) {
        if (!(arrowObject instanceof EntityArrow)) return;
        EntityArrow arrow = (EntityArrow) arrowObject;
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
        arrow.setPosition(arrow.posX + arrow.motionX * .5D,
                arrow.posY + arrow.motionY * .5D, arrow.posZ + arrow.motionZ * .5D);
        arrow.arrowShake = 0;
    }
}