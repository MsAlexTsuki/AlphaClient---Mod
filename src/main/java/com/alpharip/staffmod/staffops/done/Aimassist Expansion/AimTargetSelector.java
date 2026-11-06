package com.alpharip.staffmod.aimassistexpansion;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.ArrayList;
import java.util.List;

/** Collects visible player targets and ranks them by the selected priority. */
public final class AimTargetSelector {
    public enum Priority { CROSSHAIR, LOWEST_ANGLE, CLOSEST }

    public List<AimTarget> collect(Minecraft mc, double range) {
        List<AimTarget> targets = new ArrayList<AimTarget>();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return targets;
        for (Object object : mc.theWorld.playerEntities) {
            if (!(object instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) object;
            if (player == mc.thePlayer || player.isDead || !player.isEntityAlive()
                    || player.isInvisible() || mc.thePlayer.getDistanceToEntity(player) > range
                    || !mc.thePlayer.canEntityBeSeen(player)) continue;
            targets.add(new AimTarget(player, player.posX, player.posY + player.getEyeHeight(), player.posZ,
                    player.posX - player.prevPosX, player.posY - player.prevPosY,
                    player.posZ - player.prevPosZ, mc.thePlayer.getDistanceToEntity(player)));
        }
        return targets;
    }
}
