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
public final class CoordinatesHud {
    private static final String MODULE_ID = "coordinates";
    private static final int X = 6;
    private static final int Y = 42;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!canRender(mc, MODULE_ID)) return;

        int x = (int) Math.floor(mc.thePlayer.posX);
        int y = (int) Math.floor(mc.thePlayer.posY);
        int z = (int) Math.floor(mc.thePlayer.posZ);
        draw(mc, MODULE_ID, "XYZ " + x + " " + y + " " + z, X, Y, 0xFFFFFFFF);
    }

    private static boolean canRender(Minecraft mc, String id) {
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.fontRendererObj == null || mc.gameSettings.hideGUI
                || mc.gameSettings.showDebugInfo) return false;
        AlphaModule module = AlphaModuleRegistry.find(id);
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    private static void draw(Minecraft mc, String id, String text, int x, int y, int color) {
        float scale = HudScaleConfig.get(id);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate((float) x, (float) y, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            mc.fontRendererObj.drawStringWithShadow(text, 0, 0, color);
        } finally {
            GlStateManager.popMatrix();
        }
    }
}
