package com.alpharip.staffmod.autosave;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Draws the save indicator only while the player is in singleplayer. */
public final class AutoSaveHud {
    private final AutoSaveSettings settings;
    private final AutoSaveState state;

    AutoSaveHud(AutoSaveSettings settings, AutoSaveState state) {
        this.settings = settings;
        this.state = state;
    }

    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event == null || event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!settings.isEnabled() || !settings.isShowSaveState()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isSingleplayer() || mc.thePlayer == null || mc.theWorld == null
                || mc.fontRendererObj == null || mc.gameSettings == null
                || mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo) return;

        long now = System.currentTimeMillis();
        if (!state.hasRecentSave(now, settings.getRecentSaveWindowSeconds())) return;

        String text = AutoSaveFormatter.createHudText(state, now);
        GlStateManager.pushMatrix();
        try {
            float scale = settings.getScale();
            GlStateManager.translate(settings.getX(), settings.getY(), 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            mc.fontRendererObj.drawStringWithShadow(text, 0, 0, 0xFFFFFFFF);
        } finally {
            GlStateManager.popMatrix();
        }
    }
}
