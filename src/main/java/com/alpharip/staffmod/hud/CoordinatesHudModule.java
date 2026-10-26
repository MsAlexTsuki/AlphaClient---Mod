package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Displays current block coordinates. */
public final class CoordinatesHudModule {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("coordinates-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        mc.fontRendererObj.drawStringWithShadow(
                String.format("XYZ: %d, %d, %d", (int) Math.floor(mc.thePlayer.posX),
                        (int) Math.floor(mc.thePlayer.posY), (int) Math.floor(mc.thePlayer.posZ)),
                6, 90, 0xFFFFFFFF);
    }
}
