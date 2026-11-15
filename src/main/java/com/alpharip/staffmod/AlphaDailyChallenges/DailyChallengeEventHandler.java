package com.alpharip.staffmod.AlphaDailyChallenges;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Collects supported client-session activity. The handler is registered by the module facade. */
final class DailyChallengeEventHandler {
    private static final int TICKS_PER_MINUTE = 1200;
    private final AlphaDailyChallenges module;
    private int playtimeTicks;

    DailyChallengeEventHandler(AlphaDailyChallenges module) {
        this.module = module;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
            playtimeTicks = 0;
            return;
        }
        if (mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame()) return;

        module.getManager().activateProfile(mc.thePlayer.getUniqueID().toString());
        playtimeTicks++;
        if (playtimeTicks >= TICKS_PER_MINUTE) {
            playtimeTicks = 0;
            module.getManager().record(ChallengeMetric.PLAY_MINUTES, 1);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.player;
        if (isActivePlayer(player)) {
            module.getManager().record(ChallengeMetric.BLOCKS_BROKEN, 1);
        }
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (isActivePlayer(event.player) && event.crafting != null && event.crafting.stackSize > 0) {
            module.getManager().record(ChallengeMetric.ITEMS_CRAFTED, event.crafting.stackSize);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Entity killer = event.source == null ? null : event.source.getEntity();
        if (killer instanceof EntityPlayer && event.entityLiving instanceof IMob
                && isActivePlayer((EntityPlayer) killer)) {
            module.getManager().record(ChallengeMetric.HOSTILE_MOBS_KILLED, 1);
        }
    }

    private boolean isActivePlayer(EntityPlayer player) {
        if (player == null) return false;
        String activeId = module.getManager().getActiveProfileId();
        return activeId != null && activeId.equals(player.getUniqueID().toString());
    }
}
