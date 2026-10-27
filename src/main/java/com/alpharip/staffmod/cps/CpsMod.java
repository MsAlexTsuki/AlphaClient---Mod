// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.cps;

import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.config.HudScaleConfig;
import net.minecraft.client.renderer.GlStateManager;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

/** CPS HUD with a one-second rolling click window for both mouse buttons. */
public final class CpsMod {
    public static final int MODE_BOTH = 0;
    public static final int MODE_LEFT = 1;
    public static final int MODE_RIGHT = 2;

    private static final String[] COLOR_NAMES = {"White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Rainbow"};
    private static final int[] COLORS = {0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA};
    private static final Deque<Long> leftClicks = new ArrayDeque<Long>();
    private static final Deque<Long> rightClicks = new ArrayDeque<Long>();
    private static boolean showBackground = true;
    private static int hudX = 5, hudY = 5, colorIndex, displayMode = MODE_BOTH;
    private static boolean loaded;

    public CpsMod() { }

    public static void load() {
        if (loaded) return;
        HudPositionConfig.load();
        hudX = HudPositionConfig.getCpsX();
        hudY = HudPositionConfig.getCpsY();
        colorIndex = HudPositionConfig.getCpsColorIndex();
        if (colorIndex < 0 || colorIndex >= COLOR_NAMES.length) colorIndex = 0;
        displayMode = HudPositionConfig.getCpsDisplayMode();
        if (displayMode < MODE_BOTH || displayMode > MODE_RIGHT) displayMode = MODE_BOTH;
        showBackground = HudPositionConfig.isCpsShowBackground();
        loaded = true;
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen != null || mc.thePlayer == null) return;
        if (!org.lwjgl.input.Mouse.getEventButtonState()) return;
        int button = org.lwjgl.input.Mouse.getEventButton();
        long now = System.currentTimeMillis();
        if (button == 0) leftClicks.addLast(now);
        else if (button == 1) rightClicks.addLast(now);
        trim(leftClicks, now);
        trim(rightClicks, now);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long now = System.currentTimeMillis();
        trim(leftClicks, now);
        trim(rightClicks, now);
    }

    private static void trim(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() >= 1000L) clicks.removeFirst();
    }

    public static int getLeftCps() { trim(leftClicks, System.currentTimeMillis()); return leftClicks.size(); }
    public static int getRightCps() { trim(rightClicks, System.currentTimeMillis()); return rightClicks.size(); }
    public static int getDisplayMode() { load(); return displayMode; }
    public static String getDisplayModeName() { load(); return displayMode == MODE_LEFT ? "LMB" : displayMode == MODE_RIGHT ? "RMB" : "LMB + RMB"; }
    public static void setDisplayMode(int mode) {
        load();
        displayMode = Math.max(MODE_BOTH, Math.min(MODE_RIGHT, mode));
        HudPositionConfig.setCpsDisplayMode(displayMode);
    }
    public static boolean isShowBackground() { load(); return showBackground; }
    public static void setShowBackground(boolean value) { load(); showBackground = value; HudPositionConfig.setCpsShowBackground(value); }
    public static String getColorName() { load(); return COLOR_NAMES[colorIndex]; }
    public static void cycleColor() { load(); colorIndex = (colorIndex + 1) % COLOR_NAMES.length; HudPositionConfig.setCpsColorIndex(colorIndex); }
    public static int getTextColor() {
        load();
        if (colorIndex == COLOR_NAMES.length - 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
        }
        return COLORS[colorIndex];
    }
    public static int getHudX() { load(); return hudX; }
    public static int getHudY() { load(); return hudY; }
    public static void setHudPosition(int x, int y) {
        load(); hudX = Math.max(0, x); hudY = Math.max(0, y);
        HudPositionConfig.setCpsPosition(hudX, hudY);
    }

    public static String getDisplayText() {
        int left = getLeftCps(), right = getRightCps();
        if (getDisplayMode() == MODE_LEFT) return "CPS " + left;
        if (getDisplayMode() == MODE_RIGHT) return "CPS " + right;
        return "LMB " + left + " | RMB " + right;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("cps");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        String line = getDisplayText();
        float scale = HudScaleConfig.get("cps");
        GlStateManager.pushMatrix();
        GlStateManager.translate(hudX, hudY, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        int width = mc.fontRendererObj.getStringWidth(line) + 10;
        if (showBackground) {
            Gui.drawRect(-3, -3, width, mc.fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            Gui.drawRect(-3, -3, width, -2, 0xFFFFD866);
        }
        mc.fontRendererObj.drawStringWithShadow(line, 0, 0, getTextColor());
        GlStateManager.popMatrix();
    }
}
