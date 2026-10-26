package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Compact, read-only in-game status dashboard for common client state. */
public final class StatusDashboardHud {
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) return;
        AlphaModule module = AlphaModuleRegistry.find("status-dashboard");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        String server = mc.getCurrentServerData() == null
                ? "Singleplayer" : mc.getCurrentServerData().serverIP;
        if (server.length() > 24) server = server.substring(0, 21) + "...";
        String line = "ALPHA  " + Minecraft.getDebugFPS() + " FPS  " + server;
        float scale = HudScaleConfig.get("status-dashboard");
        GlStateManager.pushMatrix();
        GlStateManager.translate(6.0F, 90.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        mc.fontRendererObj.drawStringWithShadow(line, 0, 0, 0xFFFFFFFF);
        GlStateManager.popMatrix();
    }
}
