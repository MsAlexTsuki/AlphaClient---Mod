package com.alpharip.staffmod.BiomeHud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Reads the player's current biome and renders its name as a configurable HUD element. */
public final class BiomeHudRenderer {
    private final BiomeHudSettings settings;

    BiomeHudRenderer(BiomeHudSettings settings) {
        this.settings = settings;
    }

    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !settings.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null
                || mc.fontRendererObj == null || mc.gameSettings == null
                || mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo) {
            return;
        }

        BlockPos playerPosition = new BlockPos(
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        String biomeName = mc.theWorld.getBiomeGenForCoords(playerPosition).biomeName;
        if (biomeName == null || biomeName.trim().isEmpty()) return;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate((float) settings.getX(), (float) settings.getY(), 0.0F);
            float scale = settings.getScale();
            GlStateManager.scale(scale, scale, 1.0F);
            mc.fontRendererObj.drawStringWithShadow("Biome: " + biomeName, 0, 0, 0xFFFFFFFF);
        } finally {
            GlStateManager.popMatrix();
        }
    }
}
