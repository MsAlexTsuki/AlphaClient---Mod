package com.alpharip.staffmod.fps;

import net.minecraft.client.Minecraft;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Compact FPS counter with optimization tied to the module's enabled state. */
public final class FpsMod {
    private static boolean showFps = true;
    private static boolean showFrameTime = true;
    private static boolean showBackground = true;
    private static boolean optimizationEnabled = false;
    private static boolean rememberOriginals;
    private static boolean originalVsync;
    private static int originalLimit;
    private static int originalParticles;
    private static boolean originalFancyGraphics;
    private static long lastSampleNanos;
    private static double minFrameTimeMs;
    private static long sampleCount;

    public FpsMod() { }

    public static boolean isShowFps() { return showFps; }
    public static boolean isShowFrameTime() { return showFrameTime; }
    public static boolean isShowBackground() { return showBackground; }
    public static boolean isOptimizationEnabled() { return optimizationEnabled; }
    public static void setShowFps(boolean value) { showFps = value; }
    public static void setShowFrameTime(boolean value) { showFrameTime = value; }
    public static void setShowBackground(boolean value) { showBackground = value; }

    public static void setOptimizationEnabled(boolean enabled) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || optimizationEnabled == enabled) return;
        GameSettings settings = mc.gameSettings;
        if (enabled) {
            originalVsync = settings.enableVsync;
            originalLimit = settings.limitFramerate;
            originalParticles = settings.particleSetting;
            originalFancyGraphics = settings.fancyGraphics;
            rememberOriginals = true;
            settings.enableVsync = false;
            settings.limitFramerate = 260;
            settings.particleSetting = 2;
            settings.fancyGraphics = false;
            settings.saveOptions();
        } else if (rememberOriginals) {
            settings.enableVsync = originalVsync;
            settings.limitFramerate = originalLimit;
            settings.particleSetting = originalParticles;
            settings.fancyGraphics = originalFancyGraphics;
            settings.saveOptions();
        }
        optimizationEnabled = enabled;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        AlphaModule module = AlphaModuleRegistry.find("fps");
        boolean moduleEnabled = module != null && module.isEnabled();
        // Optimization follows the module state; merely opening its settings does nothing.
        setOptimizationEnabled(moduleEnabled);

        if (!moduleEnabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.gameSettings.showDebugInfo) return;
        long now = System.nanoTime();
        if (lastSampleNanos != 0L) {
            double frameMs = (now - lastSampleNanos) / 1000000.0D;
            if (frameMs > 0.0D && frameMs < 1000.0D) {
                minFrameTimeMs = sampleCount == 0 ? frameMs : Math.min(minFrameTimeMs, frameMs);
                sampleCount++;
            }
        }
        lastSampleNanos = now;
        int fps = Minecraft.getDebugFPS();
        if (!showFps && !showFrameTime) return;
        String line = (showFps ? "FPS " + fps : "")
                + (showFps && showFrameTime ? "  " : "")
                + (showFrameTime ? "MIN " + (sampleCount == 0 ? "--" : String.format(java.util.Locale.ROOT, "%.1f", minFrameTimeMs)) + " ms" : "");
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
