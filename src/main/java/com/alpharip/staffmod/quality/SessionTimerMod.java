package com.alpharip.staffmod.quality;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Tracks elapsed time while the player remains in the current world. */
public final class SessionTimerMod {
    private long startedAt;
    private Object worldReference;

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("session-timer");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        if (mc.theWorld == null || mc.thePlayer == null) {
            startedAt = 0L;
            worldReference = null;
            return;
        }
        if (worldReference != mc.theWorld || startedAt == 0L) {
            worldReference = mc.theWorld;
            startedAt = System.currentTimeMillis();
        }
        long seconds = Math.max(0L, (System.currentTimeMillis() - startedAt) / 1000L);
        mc.fontRendererObj.drawStringWithShadow(String.format("Session: %02d:%02d:%02d",
                seconds / 3600L, (seconds / 60L) % 60L, seconds % 60L), 6, 102, 0xFFFFFFFF);
    }
}
