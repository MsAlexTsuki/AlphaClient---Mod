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
public final class CompassHud {
    private static final String MODULE_ID = "compass";
    private static final String[] DIRECTIONS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!canRender(mc)) return;

        float yaw = mc.thePlayer.rotationYaw % 360.0F;
        if (yaw < 0.0F) yaw += 360.0F;
        int directionIndex = Math.round(yaw / 45.0F) % DIRECTIONS.length;
        String text = "DIR " + DIRECTIONS[directionIndex] + "  " + Math.round(yaw) + "\u00b0";
        draw(mc, text);
    }

    private static boolean canRender(Minecraft mc) {
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.fontRendererObj == null || mc.gameSettings.hideGUI
                || mc.gameSettings.showDebugInfo) return false;
        AlphaModule module = AlphaModuleRegistry.find(MODULE_ID);
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    private static void draw(Minecraft mc, String text) {
        float scale = HudScaleConfig.get(MODULE_ID);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(6.0F, 54.0F, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            mc.fontRendererObj.drawStringWithShadow(text, 0, 0, 0xFFFFFFFF);
        } finally {
            GlStateManager.popMatrix();
        }
    }
}
