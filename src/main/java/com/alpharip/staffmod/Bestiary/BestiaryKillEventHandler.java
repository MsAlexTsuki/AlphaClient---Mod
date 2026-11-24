package com.alpharip.staffmod.Bestiary;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Records client-observed deaths only when the local player receives kill credit. */
final class BestiaryKillEventHandler {
    private final BestiaryTracker tracker;

    BestiaryKillEventHandler(BestiaryTracker tracker) {
        if (tracker == null) throw new IllegalArgumentException("tracker must not be null");
        this.tracker = tracker;
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event == null || event.entityLiving == null || event.source == null) return;
        EntityLivingBase victim = event.entityLiving;
        if (victim.worldObj == null || !victim.worldObj.isRemote) return;
        if (victim instanceof EntityPlayer) return;

        Entity attacker = event.source.getEntity();
        if (!(attacker instanceof EntityPlayer)) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null || attacker != minecraft.thePlayer) return;

        String registryId = EntityList.getEntityString(victim);
        if (registryId == null || registryId.trim().isEmpty()) return;
        String worldName = victim.worldObj.getWorldInfo() == null
                ? "Unknown World" : victim.worldObj.getWorldInfo().getWorldName();
        tracker.onMobKilled(registryId, victim.getDisplayName().getFormattedText(),
                worldName, System.currentTimeMillis());
    }
}
