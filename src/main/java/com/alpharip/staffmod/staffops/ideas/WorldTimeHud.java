package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Displays in-game world time using the 20-ticks-per-second Minecraft clock. */
public final class WorldTimeHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("world-time-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        long ticks = mc.theWorld.getWorldTime() % 24000L;
        int hours = (int) ((ticks / 1000L + 6L) % 24L);
        int minutes = (int) ((ticks % 1000L) * 60L / 1000L);
        mc.fontRendererObj.drawStringWithShadow(String.format("World time: %02d:%02d", hours, minutes), 6, 174, 0xFFFFFFFF);
    }
}
