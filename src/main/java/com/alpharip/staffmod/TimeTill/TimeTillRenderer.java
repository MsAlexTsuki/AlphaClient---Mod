package com.alpharip.staffmod.TimeTill;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Draws the selected sunset or night countdown on the in-game HUD. */
public final class TimeTillRenderer {
    private final TimeTillSettings settings;

    TimeTillRenderer(TimeTillSettings settings) {
        this.settings = settings;
    }

    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !settings.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.fontRendererObj == null
                || mc.gameSettings == null || mc.gameSettings.hideGUI
                || mc.gameSettings.showDebugInfo) {
            return;
        }

        TimeTillSettings.Target target = settings.getTarget();
        long remainingTicks = TimeTillFormatter.ticksUntil(mc.theWorld.getWorldTime(), target);
        String label = target == TimeTillSettings.Target.NIGHT ? "Night in: " : "Sunset in: ";
        String text = label + TimeTillFormatter.format(remainingTicks);

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
