package com.alpharip.staffmod.quality;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Tracks elapsed time for the current game process; persistence can be added later. */
public final class PlaytimeTrackerMod {
    private final long processStartedAt = System.currentTimeMillis();

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("playtime-tracker");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        long seconds = Math.max(0L, (System.currentTimeMillis() - processStartedAt) / 1000L);
        mc.fontRendererObj.drawStringWithShadow(String.format("Playtime: %02d:%02d:%02d",
                seconds / 3600L, (seconds / 60L) % 60L, seconds % 60L), 6, 114, 0xFFFFFFFF);
    }
}
