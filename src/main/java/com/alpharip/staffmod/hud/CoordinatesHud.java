package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** General HUD showing the local player's block coordinates. */
public final class CoordinatesHud {
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) return;
        AlphaModule module = AlphaModuleRegistry.find("coordinates");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        int x = (int)Math.floor(mc.thePlayer.posX);
        int y = (int)Math.floor(mc.thePlayer.posY);
        int z = (int)Math.floor(mc.thePlayer.posZ);
        String line = "XYZ " + x + " " + y + " " + z;
        float scale = HudScaleConfig.get("coordinates");
        GlStateManager.pushMatrix();
        GlStateManager.translate(6.0F, 42.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        mc.fontRendererObj.drawStringWithShadow(line, 0, 0, 0xFFFFFFFF);
        GlStateManager.popMatrix();
    }
}
