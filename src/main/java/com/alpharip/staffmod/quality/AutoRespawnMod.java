package com.alpharip.staffmod.quality;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Automatically respawns the local player when the enabled module detects death. */
public final class AutoRespawnMod {
    private long lastRespawnAt;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return;

        AlphaModule module = AlphaModuleRegistry.find("auto-respawn");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        if (!mc.thePlayer.isDead && mc.thePlayer.getHealth() > 0.0F) return;

        long now = System.currentTimeMillis();
        if (now - lastRespawnAt < 1000L) return;
        lastRespawnAt = now;
        mc.thePlayer.respawnPlayer();
    }
}
