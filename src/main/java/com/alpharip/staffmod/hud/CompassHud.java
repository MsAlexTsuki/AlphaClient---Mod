package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** General compass HUD using the player's current yaw. */
public final class CompassHud {
    private static final String[] DIRECTIONS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) return;
        AlphaModule module = AlphaModuleRegistry.find("compass");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        float yaw = mc.thePlayer.rotationYaw % 360.0F;
        if (yaw < 0.0F) yaw += 360.0F;
        int index = Math.round(yaw / 45.0F) % DIRECTIONS.length;
        String line = "DIR " + DIRECTIONS[index] + "  " + Math.round(yaw) + "\u00b0";
        float scale = HudScaleConfig.get("compass");
        GlStateManager.pushMatrix();
        GlStateManager.translate(6.0F, 54.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        mc.fontRendererObj.drawStringWithShadow(line, 0, 0, 0xFFFFFFFF);
        GlStateManager.popMatrix();
    }
}
