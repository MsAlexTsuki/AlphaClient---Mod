package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Base rendering pattern for an AlphaClient 1.8.9 HUD.
 * Each HUD remains a separate module and uses its own registry ID and scale setting.
 */
public final class StatusDashboardHud {
    private static final String MODULE_ID = "status-dashboard";

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.fontRendererObj == null || mc.gameSettings.hideGUI
                || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find(MODULE_ID);
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        String server = "Singleplayer";
        if (mc.getCurrentServerData() != null) {
            server = mc.getCurrentServerData().serverIP;
            if (server == null || server.isEmpty()) server = "Multiplayer";
        }
        if (server.length() > 24) server = server.substring(0, 21) + "...";
        String text = "ALPHA  " + Minecraft.getDebugFPS() + " FPS  " + server;

        float scale = HudScaleConfig.get(MODULE_ID);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(6.0F, 90.0F, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            mc.fontRendererObj.drawStringWithShadow(text, 0, 0, 0xFFFFFFFF);
        } finally {
            GlStateManager.popMatrix();
        }
    }
}
