package com.alpharip.staffmod.fps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Compact FPS counter with automatic performance settings for AlphaClient. */
public final class FpsMod {
    private static boolean showFps = true;
    private static boolean showBackground = true;
    private static boolean originalsCaptured;
    private static boolean originalVsync;
    private static int originalLimit;
    private static int originalParticles;
    private static boolean originalFancyGraphics;
    private static boolean optimizationApplied;

    public FpsMod() { }

    public static boolean isShowFps() { return showFps; }
    public static boolean isShowBackground() { return showBackground; }
    public static void setShowFps(boolean value) { showFps = value; }
    public static void setShowBackground(boolean value) { showBackground = value; }

    /** Applies conservative FPS settings automatically, independently of HUD/module toggles. */
    public static void applyAutomaticOptimization() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || optimizationApplied) return;
        GameSettings settings = mc.gameSettings;
        originalVsync = settings.enableVsync;
        originalLimit = settings.limitFramerate;
        originalParticles = settings.particleSetting;
        originalFancyGraphics = settings.fancyGraphics;
        originalsCaptured = true;

        settings.enableVsync = false;
        settings.limitFramerate = 260;
        settings.particleSetting = 2;
        settings.fancyGraphics = false;
        settings.saveOptions();
        optimizationApplied = true;
    }

    /** Restores the user's settings when the client is shut down normally. */
    public static void restoreOriginalSettings() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || !optimizationApplied || !originalsCaptured) return;
        GameSettings settings = mc.gameSettings;
        settings.enableVsync = originalVsync;
        settings.limitFramerate = originalLimit;
        settings.particleSetting = originalParticles;
        settings.fancyGraphics = originalFancyGraphics;
        settings.saveOptions();
        optimizationApplied = false;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.gameSettings.showDebugInfo) return;
        if (!showFps) return;

        String line = "FPS " + Minecraft.getDebugFPS();
        int x = 6;
        int y = 6;
        int width = mc.fontRendererObj.getStringWidth(line) + 10;
        if (showBackground) {
            net.minecraft.client.gui.Gui.drawRect(x - 3, y - 3, x + width, y + mc.fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            net.minecraft.client.gui.Gui.drawRect(x - 3, y - 3, x + width, y - 2, 0xFFFFD866);
        }
        mc.fontRendererObj.drawStringWithShadow(line, x, y, 0xFFFFFFFF);
    }
}
