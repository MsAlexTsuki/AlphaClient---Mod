package com.alpharip.staffmod.fishingbitealarm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Samples the local player's active fishing hook once per client tick. */
final class FishingBiteAlarmEventHandler {
    private final FishingBiteAlarm module;

    FishingBiteAlarmEventHandler(FishingBiteAlarm module) {
        if (module == null) throw new IllegalArgumentException("module");
        this.module = module;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc == null ? null : mc.thePlayer;
        if (player == null || mc.theWorld == null) {
            module.onNoActiveHook();
            return;
        }

        EntityFishHook hook = player.fishEntity;
        if (hook == null || hook.isDead || hook.angler != player) {
            module.onNoActiveHook();
            return;
        }

        module.onHookSample(FishingHookSample.from(hook));
    }
}
