package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Shows current accumulated fall distance for basic movement debugging. */
public final class FallDistanceHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("fall-distance-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        mc.fontRendererObj.drawStringWithShadow(String.format("Fall distance: %.1f", mc.thePlayer.fallDistance), 6, 186, 0xFFFFFFFF);
    }
}
