package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Minimal, real HUD features used by the initial AlphaClient core. */
public final class AlphaHudRenderer {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.thePlayer == null) {
            return;
        }
        AlphaModule hud = AlphaModuleRegistry.find("alpha_hud");
        if (hud != null && hud.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow("ALPHA CLIENT",
                    6, 6, 0xFFFFD866);
            mc.fontRendererObj.drawStringWithShadow(PermissionService.getStatusText(),
                    6, 17, 0xFFB8BBC4);
        }
        AlphaModule fps = AlphaModuleRegistry.find("fps_counter");
        if (fps != null && fps.isEnabled()) {
            mc.fontRendererObj.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS(),
                    6, hud != null && hud.isEnabled() ? 29 : 6, 0xFFFFFFFF);
        }
    }
}