package com.alpharip.staffmod.fps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import java.awt.Color;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Compact FPS counter with automatic performance settings for AlphaClient. */
public final class FpsMod {
    private static boolean showFps = true;
    private static boolean showBackground = true;
    private static int hudX = 6;
    private static int hudY = 6;
    private static int colorIndex = 0;
    private static final String[] COLOR_NAMES = {"White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Rainbow"};
    private static final int[] COLORS = {0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA};
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
    public static int getHudX() { return hudX; }
    public static int getHudY() { return hudY; }
    public static void setHudPosition(int x, int y) { hudX = x; hudY = y; }
    public static String getColorName() { return COLOR_NAMES[colorIndex]; }
    public static void cycleColor() { colorIndex = (colorIndex + 1) % COLOR_NAMES.length; }
    public static int getTextColor() {
        if (colorIndex == COLOR_NAMES.length - 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
        }
        return COLORS[colorIndex];
    }

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
        mc.fontRendererObj.drawStringWithShadow(line, x, y, getTextColor());
    }
}
