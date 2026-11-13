package com.alpharip.staffmod.ArrowCounter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Renders the current arrow total as a small HUD element. */
public final class ArrowCounterRenderer {
    private final ArrowCounterSettings settings;
    private final ArrowCounterScanner scanner;

    ArrowCounterRenderer(ArrowCounterSettings settings, ArrowCounterScanner scanner) {
        this.settings = settings;
        this.scanner = scanner;
    }

    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !settings.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null
                || mc.gameSettings == null || mc.gameSettings.hideGUI
                || mc.gameSettings.showDebugInfo) {
            return;
        }

        String text = "Arrows: " + scanner.getArrowCount();
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate((float) settings.getX(), (float) settings.getY(), 0.0F);
            float scale = settings.getScale();
            GlStateManager.scale(scale, scale, 1.0F);
            mc.fontRendererObj.drawStringWithShadow(text, 0, 0, 0xFFFFFFFF);
        } finally {
            GlStateManager.popMatrix();
        }
    }
}
