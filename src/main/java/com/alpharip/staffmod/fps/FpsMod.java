package com.alpharip.staffmod.fps;

import net.minecraft.client.Minecraft;
import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import java.awt.Color;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Compact FPS HUD renderer for AlphaClient. */
public final class FpsMod {
    private static boolean showFps = true;
    private static boolean showBackground = true;
    private static int hudX = 6;
    private static int hudY = 6;
    private static boolean hudPositionLoaded;
    private static int colorIndex = 0;
    private static final String[] COLOR_NAMES = {"White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Rainbow"};
    private static final int[] COLORS = {0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA};

    public FpsMod() { }

    public static boolean isShowFps() { return showFps; }
    public static boolean isShowBackground() { return showBackground; }
    public static void setShowFps(boolean value) { showFps = value; }
    public static void setShowBackground(boolean value) { showBackground = value; }
    public static int getHudX() { return hudX; }
    public static int getHudY() { return hudY; }
    public static void loadHudPosition() {
        if (hudPositionLoaded) return;
        HudPositionConfig.load();
        hudX = HudPositionConfig.getFpsX();
        hudY = HudPositionConfig.getFpsY();
        colorIndex = HudPositionConfig.getFpsColorIndex();
        if (colorIndex < 0 || colorIndex >= COLOR_NAMES.length) colorIndex = 0;
        hudPositionLoaded = true;
    }
    public static void setHudPosition(int x, int y) {
        loadHudPosition();
        hudX = Math.max(0, x);
        hudY = Math.max(0, y);
        HudPositionConfig.setFpsPosition(hudX, hudY);
    }
    public static String getColorName() {
        loadHudPosition();
        return COLOR_NAMES[colorIndex];
    }
    public static void cycleColor() {
        loadHudPosition();
        colorIndex = (colorIndex + 1) % COLOR_NAMES.length;
        HudPositionConfig.setFpsColorIndex(colorIndex);
    }
    public static int getTextColor() {
        loadHudPosition();
        if (colorIndex == COLOR_NAMES.length - 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
        }
        return COLORS[colorIndex];
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        loadHudPosition();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("fps");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        if (!showFps) return;

        String line = "FPS " + Minecraft.getDebugFPS();
        int x = hudX;
        int y = hudY;
        int width = mc.fontRendererObj.getStringWidth(line) + 10;
        if (showBackground) {
            net.minecraft.client.gui.Gui.drawRect(x - 3, y - 3, x + width, y + mc.fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            net.minecraft.client.gui.Gui.drawRect(x - 3, y - 3, x + width, y - 2, 0xFFFFD866);
        }
        mc.fontRendererObj.drawStringWithShadow(line, x, y, getTextColor());
    }
}
