// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.keystrokes;

import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Lunar-style movement and mouse input HUD, using AlphaClient's fixed color palette. */
public final class KeystrokesMod {
    private static final int GOLD = 0xFFFFC83D;
    private static final int DARK = 0xE60A0C11;
    private static final int PRESSED = 0xF0FFC83D;
    private static final int WHITE = 0xFFF3F4F6;
    private static final int MUTED = 0xFFB8BDC8;
    private static final String CONFIG_NAME = "alphaclient-keystrokes.properties";

    private static boolean loaded;
    private static boolean showClicks = true;
    private static boolean showCps;
    private static boolean showSpaceBar = true;
    private static boolean showMovementKeys = true;
    private static boolean useArrows;
    private static boolean border;
    private static boolean innerBorder;
    private static boolean textShadow;
    private static int fadeDelay = 75;
    private static int boxSize = 18;
    private static float borderThickness = 1.0F;
    private static int hudX = 5;
    private static int hudY = 5;
    private static float scale = 1.0F;

    private long wPressedAt, aPressedAt, sPressedAt, dPressedAt, spacePressedAt;
    private long leftPressedAt, rightPressedAt;
    private boolean previousLeft, previousRight;

    public static void load() {
        if (loaded) return;
        loaded = true;
        File file = getConfigFile();
        if (!file.isFile()) { save(); return; }
        Properties p = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            p.load(in);
            showClicks = bool(p, "showClicks", true);
            showCps = bool(p, "showCps", false);
            showSpaceBar = bool(p, "showSpaceBar", true);
            showMovementKeys = bool(p, "showMovementKeys", true);
            useArrows = bool(p, "useArrows", false);
            border = bool(p, "border", false);
            innerBorder = bool(p, "innerBorder", false);
            textShadow = bool(p, "textShadow", false);
            fadeDelay = clamp(integer(p, "fadeDelay", 75), 0, 500);
            boxSize = clamp(integer(p, "boxSize", 18), 10, 32);
            borderThickness = Math.max(0.5F, Math.min(3.0F, decimal(p, "borderThickness", 1.0F)));
            hudX = Math.max(0, integer(p, "hudX", 5));
            hudY = Math.max(0, integer(p, "hudY", 5));
            scale = Math.max(0.25F, Math.min(5.0F, decimal(p, "scale", 1.0F)));
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not load Keystrokes settings: " + e.getMessage());
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) { }
        }
    }

    public static void save() {
        File file = getConfigFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        Properties p = new Properties();
        p.setProperty("showClicks", String.valueOf(showClicks));
        p.setProperty("showCps", String.valueOf(showCps));
        p.setProperty("showSpaceBar", String.valueOf(showSpaceBar));
        p.setProperty("showMovementKeys", String.valueOf(showMovementKeys));
        p.setProperty("useArrows", String.valueOf(useArrows));
        p.setProperty("border", String.valueOf(border));
        p.setProperty("innerBorder", String.valueOf(innerBorder));
        p.setProperty("textShadow", String.valueOf(textShadow));
        p.setProperty("fadeDelay", String.valueOf(fadeDelay));
        p.setProperty("boxSize", String.valueOf(boxSize));
        p.setProperty("borderThickness", String.valueOf(borderThickness));
        p.setProperty("hudX", String.valueOf(hudX));
        p.setProperty("hudY", String.valueOf(hudY));
        p.setProperty("scale", String.valueOf(scale));
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            p.store(out, "AlphaClient Keystrokes settings");
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save Keystrokes settings: " + e.getMessage());
        } finally {
            if (out != null) try { out.close(); } catch (Exception ignored) { }
        }
    }

    private static File getConfigFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), CONFIG_NAME);
    }
    private static boolean bool(Properties p, String key, boolean fallback) {
        return Boolean.parseBoolean(p.getProperty(key, String.valueOf(fallback)));
    }
    private static int integer(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, String.valueOf(fallback))); }
        catch (Exception ignored) { return fallback; }
    }
    private static float decimal(Properties p, String key, float fallback) {
        try { return Float.parseFloat(p.getProperty(key, String.valueOf(fallback))); }
        catch (Exception ignored) { return fallback; }
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || !mc.inGameHasFocus || mc.currentScreen != null
                || !Mouse.getEventButtonState()) return;
        long now = System.currentTimeMillis();
        int button = Mouse.getEventButton();
        if (button == 0) leftPressedAt = now;
        else if (button == 1) rightPressedAt = now;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || !mc.inGameHasFocus || mc.currentScreen != null) {
            previousLeft = false;
            previousRight = false;
            return;
        }
        long now = System.currentTimeMillis();
        if (mc.gameSettings.keyBindForward.isKeyDown()) wPressedAt = now;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) aPressedAt = now;
        if (mc.gameSettings.keyBindBack.isKeyDown()) sPressedAt = now;
        if (mc.gameSettings.keyBindRight.isKeyDown()) dPressedAt = now;
        if (mc.gameSettings.keyBindJump.isKeyDown()) spacePressedAt = now;
        boolean left = Mouse.isButtonDown(0);
        boolean right = Mouse.isButtonDown(1);
        if (left && !previousLeft) leftPressedAt = now;
        if (right && !previousRight) rightPressedAt = now;
        previousLeft = left;
        previousRight = right;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        AlphaModule module = AlphaModuleRegistry.find("keystrokes");
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings.showDebugInfo
                || module == null || !module.isEnabled() || !module.isUnlocked()) return;
        render(mc, hudX, hudY, scale, false);
    }

    public static void renderEditorPreview() {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) render(mc, hudX, hudY, scale, true);
    }

    private static void render(Minecraft mc, int x, int y, float renderScale, boolean preview) {
        if (mc.fontRendererObj == null) return;
        int size = boxSize;
        int gap = Math.max(1, size / 9);
        int gridWidth = size * 3 + gap * 2;
        int mouseHeight = getMouseHeight(size);
        int movementTop = showClicks ? mouseHeight + gap : 0;
        int spaceTop = movementTop + (showMovementKeys ? size * 2 + gap * 2 : 0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(renderScale, renderScale, 1.0F);

        if (showClicks) {
            int mouseWidth = (gridWidth - gap) / 2;
            drawKey(mc, 0, 0, mouseWidth, mouseHeight, "LMB",
                    pressedAlpha(isPressed(mc, 0), leftPressedAt), showCps ? CpsMod.getLeftCps() : -1);
            drawKey(mc, mouseWidth + gap, 0, mouseWidth, mouseHeight, "RMB",
                    pressedAlpha(isPressed(mc, 1), rightPressedAt), showCps ? CpsMod.getRightCps() : -1);
        }

        if (showMovementKeys) {
            int top = movementTop;
            int wX = size + gap;
            drawKey(mc, wX, top, size, size, useArrows ? "↑" : "W",
                    pressedAlpha(mc.gameSettings.keyBindForward.isKeyDown(),  wPressedAt), -1);
            drawKey(mc, 0, top + size + gap, size, size, useArrows ? "←" : "A",
                    pressedAlpha(mc.gameSettings.keyBindLeft.isKeyDown(), aPressedAt), -1);
            drawKey(mc, size + gap, top + size + gap, size, size, useArrows ? "↓" : "S",
                    pressedAlpha(mc.gameSettings.keyBindBack.isKeyDown(), sPressedAt), -1);
            drawKey(mc, (size + gap) * 2, top + size + gap, size, size, useArrows ? "→" : "D",
                    pressedAlpha(mc.gameSettings.keyBindRight.isKeyDown(), dPressedAt), -1);
        }

        if (showSpaceBar) {
            int top = spaceTop;
            int height = Math.max(4, size / 2);
            drawKey(mc, 0, top, gridWidth, height, "SPACE",
                    pressedAlpha(mc.gameSettings.keyBindJump.isKeyDown(), spacePressedAt), -1);
        }
        GlStateManager.popMatrix();
    }

    private static boolean isPressed(Minecraft mc, int button) {
        return mc != null && mc.thePlayer != null && mc.inGameHasFocus && mc.currentScreen == null
                && Mouse.isButtonDown(button);
    }

    private static float pressedAlpha(boolean down, long pressedAt) {
        if (down) return 1.0F;
        if (fadeDelay <= 0 || pressedAt <= 0L) return 0.0F;
        long elapsed = System.currentTimeMillis() - pressedAt;
        if (elapsed >= fadeDelay) return 0.0F;
        return 1.0F - (float) elapsed / (float) fadeDelay;
    }

    private static void drawKey(Minecraft mc, int x, int y, int width, int height,
                                String label, float active, int cps) {
        int background = blend(DARK, PRESSED, active);
        Gui.drawRect(x, y, x + width, y + height, background);
        if (border) {
            int t = Math.max(1, Math.round(borderThickness));
            Gui.drawRect(x, y, x + width, y + t, GOLD);
            Gui.drawRect(x, y + height - t, x + width, y + height, GOLD);
            Gui.drawRect(x, y, x + t, y + height, GOLD);
            Gui.drawRect(x + width - t, y, x + width, y + height, GOLD);
        }
        if (innerBorder && width > 4 && height > 4) {
            int inner = 0x66FFC83D;
            Gui.drawRect(x + 2, y + 2, x + width - 2, y + 3, inner);
            Gui.drawRect(x + 2, y + height - 3, x + width - 2, y + height - 2, inner);
        }
        int color = active > 0.01F ? 0xFF111111 : WHITE;
        if (cps >= 0) {
            drawCentered(mc, label, x, y + 3, width, color);
            drawCentered(mc, cps + " CPS", x, y + height - mc.fontRendererObj.FONT_HEIGHT - 2, width, color);
        } else {
            drawCentered(mc, label, x, y + (height - mc.fontRendererObj.FONT_HEIGHT) / 2, width, color);
        }
    }

    private static void drawCentered(Minecraft mc, String text, int x, int y, int width, int color) {
        int tx = x + (width - mc.fontRendererObj.getStringWidth(text)) / 2;
        if (textShadow) mc.fontRendererObj.drawStringWithShadow(text, tx, y, color);
        else mc.fontRendererObj.drawString(text, tx, y, color, false);
    }

    private static int blend(int from, int to, float amount) {
        float a = Math.max(0F, Math.min(1F, amount));
        int aa = (int)(((from >>> 24) & 255) * (1F - a) + ((to >>> 24) & 255) * a);
        int r = (int)(((from >>> 16) & 255) * (1F - a) + ((to >>> 16) & 255) * a);
        int g = (int)(((from >>> 8) & 255) * (1F - a) + ((to >>> 8) & 255) * a);
        int b = (int)((from & 255) * (1F - a) + (to & 255) * a);
        return (aa << 24) | (r << 16) | (g << 8) | b;
    }

    public static int getHudX() { load(); return hudX; }
    public static int getHudY() { load(); return hudY; }
    public static void setHudPosition(int x, int y) {
        load(); hudX = Math.max(0, x); hudY = Math.max(0, y); save();
    }
    public static float getScale() { load(); return scale; }
    public static void setScale(float value) { load(); scale = Math.max(0.25F, Math.min(5.0F, value)); save(); }
    public static int getHudWidth() {
        load(); int size = boxSize, gap = Math.max(1, size / 9);
        return size * 3 + gap * 2;
    }
    private static int getMouseHeight(int size) {
        return Math.max(size + 8, showCps ? 22 : size + 8);
    }

    public static int getHudHeight() {
        load(); int size = boxSize, gap = Math.max(1, size / 9);
        return (showClicks ? getMouseHeight(size) : 0) + (showClicks && showMovementKeys ? gap : 0)
                + (showMovementKeys ? size * 2 + gap : 0)
                + (showSpaceBar ? ((showClicks || showMovementKeys) ? gap : 0) + Math.max(4, size / 2) : 0);
    }

    public static boolean isShowClicks() { load(); return showClicks; }
    public static void setShowClicks(boolean v) { load(); showClicks = v; save(); }
    public static boolean isShowCps() { load(); return showCps; }
    public static void setShowCps(boolean v) { load(); showCps = v; save(); }
    public static boolean isShowSpaceBar() { load(); return showSpaceBar; }
    public static void setShowSpaceBar(boolean v) { load(); showSpaceBar = v; save(); }
    public static boolean isShowMovementKeys() { load(); return showMovementKeys; }
    public static void setShowMovementKeys(boolean v) { load(); showMovementKeys = v; save(); }
    public static boolean isUseArrows() { load(); return useArrows; }
    public static void setUseArrows(boolean v) { load(); useArrows = v; save(); }
    public static boolean isBorder() { load(); return border; }
    public static void setBorder(boolean v) { load(); border = v; save(); }
    public static boolean isInnerBorder() { load(); return innerBorder; }
    public static void setInnerBorder(boolean v) { load(); innerBorder = v; save(); }
    public static boolean isTextShadow() { load(); return textShadow; }
    public static void setTextShadow(boolean v) { load(); textShadow = v; save(); }
    public static int getFadeDelay() { load(); return fadeDelay; }
    public static void adjustFadeDelay(int delta) { load(); fadeDelay = clamp(fadeDelay + delta, 0, 500); save(); }
    public static int getBoxSize() { load(); return boxSize; }
    public static void adjustBoxSize(int delta) { load(); boxSize = clamp(boxSize + delta, 10, 32); save(); }
    public static float getBorderThickness() { load(); return borderThickness; }
    public static void adjustBorderThickness(float delta) { load(); borderThickness = Math.max(0.5F, Math.min(3.0F, borderThickness + delta)); save(); }
}
