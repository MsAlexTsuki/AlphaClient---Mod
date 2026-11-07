// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.keystrokes;

import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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
    private static int backgroundColor = DARK;
    private static int pressedBackgroundColor = PRESSED;
    private static int textColor = WHITE;
    private static int pressedTextColor = 0xFF111111;
    private static int borderColor = GOLD;
    private static final String CONFIG_NAME = "alphaclient-keystrokes.properties";

    private static boolean loaded;
    private static boolean showClicks = true;
    private static boolean showCps;
    private static boolean showSpaceBar = true;
    private static boolean showMovementKeys = true;
    private static boolean useArrows;
    private static boolean border;
        private static boolean textShadow;
    private static int fadeDelay = 75;
    private static int boxSize = 16;
    private static float borderThickness = 1.0F;
    private static int hudX = 5;
    private static int hudY = 5;
    private static float scale = 1.0F;

    private static long wPressedAt, aPressedAt, sPressedAt, dPressedAt, spacePressedAt;
    private static long leftPressedAt, rightPressedAt;
    private static boolean previousLeft, previousRight;

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
            textShadow = bool(p, "textShadow", false);
            fadeDelay = clamp(integer(p, "fadeDelay", 75), 0, 500);
            boxSize = clamp(integer(p, "boxSize", 16), 10, 32);
            backgroundColor = color(p, "backgroundColor", DARK);
            pressedBackgroundColor = color(p, "pressedBackgroundColor", PRESSED);
            textColor = color(p, "textColor", WHITE);
            pressedTextColor = color(p, "pressedTextColor", 0xFF111111);
            borderColor = color(p, "borderColor", GOLD);
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
        p.setProperty("textShadow", String.valueOf(textShadow));
        p.setProperty("fadeDelay", String.valueOf(fadeDelay));
        p.setProperty("boxSize", String.valueOf(boxSize));
        p.setProperty("backgroundColor", String.format("%08X", backgroundColor));
        p.setProperty("pressedBackgroundColor", String.format("%08X", pressedBackgroundColor));
        p.setProperty("textColor", String.format("%08X", textColor));
        p.setProperty("pressedTextColor", String.format("%08X", pressedTextColor));
        p.setProperty("borderColor", String.format("%08X", borderColor));
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
    private static int color(Properties p, String key, int fallback) {
        try { return (int) Long.parseLong(p.getProperty(key, String.format("%08X", fallback)), 16); }
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
        int mouseWidth = Math.max(18, Math.round((gridWidth - gap) / 2.0F * 0.72F));
        int mouseGap = gap;
        int mouseRowHeight = showClicks ? mouseHeight : 0;
        int movementTop = showMovementKeys ? (showClicks ? mouseRowHeight + gap : 0) : 0;
        int clicksTop = showMovementKeys ? size * 2 + gap : 0;
        if (showMovementKeys && showClicks) clicksTop = movementTop + size * 2 + gap;
        else if (showMovementKeys) clicksTop = size * 2 + gap;
        else clicksTop = 0;
        int afterMovement = showMovementKeys ? size * 2 + gap : 0;
        int afterClicks = showClicks ? mouseHeight + gap : 0;
        int spaceTop = (showMovementKeys ? size * 2 + gap : 0)
                + (showClicks ? mouseHeight + gap : 0);
        int contentWidth = gridWidth;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(renderScale, renderScale, 1.0F);

        if (showMovementKeys) {
            int top = 0;
            int wX = size + gap;
            drawKey(mc, wX, top, size, size, useArrows ? "↑" : "W",
                    pressedAlpha(mc.gameSettings.keyBindForward.isKeyDown(), wPressedAt), -1);
            drawKey(mc, 0, top + size + gap, size, size, useArrows ? "←" : "A",
                    pressedAlpha(mc.gameSettings.keyBindLeft.isKeyDown(), aPressedAt), -1);
            drawKey(mc, size + gap, top + size + gap, size, size, useArrows ? "↓" : "S",
                    pressedAlpha(mc.gameSettings.keyBindBack.isKeyDown(), sPressedAt), -1);
            drawKey(mc, (size + gap) * 2, top + size + gap, size, size, useArrows ? "→" : "D",
                    pressedAlpha(mc.gameSettings.keyBindRight.isKeyDown(), dPressedAt), -1);
        }

        if (showClicks) {
            int leftX = (gridWidth - (mouseWidth * 2 + mouseGap)) / 2;
            int top = (showMovementKeys ? size * 2 + gap * 2 : 0);
            drawMouseKey(mc, leftX, top, mouseWidth, mouseHeight, "LMB",
                    pressedAlpha(isPressed(mc, 0), leftPressedAt), showCps ? CpsMod.getLeftCps() : -1);
            drawMouseKey(mc, leftX + mouseWidth + mouseGap, top, mouseWidth, mouseHeight, "RMB",
                    pressedAlpha(isPressed(mc, 1), rightPressedAt), showCps ? CpsMod.getRightCps() : -1);
        }

        if (showSpaceBar) {
            int top = (showMovementKeys ? size * 2 + gap : 0)
                    + (showClicks ? mouseHeight + gap : 0);
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
        int background = blend(backgroundColor, pressedBackgroundColor, active);
        Gui.drawRect(x, y, x + width, y + height, background);
        if (border) {
            int t = Math.max(1, Math.round(borderThickness));
            Gui.drawRect(x, y, x + width, y + t, borderColor);
            Gui.drawRect(x, y + height - t, x + width, y + height, borderColor);
            Gui.drawRect(x, y, x + t, y + height, borderColor);
            Gui.drawRect(x + width - t, y, x + width, y + height, borderColor);
        }
        int color = active > 0.01F ? pressedTextColor : textColor;
        if (cps >= 0) {
            drawCentered(mc, label, x, y + 2, width, color);
            drawScaledCentered(mc, cps + " CPS", x, y + height - 8, width, color, 0.72F);
        } else {
            drawCentered(mc, label, x, y + (height - mc.fontRendererObj.FONT_HEIGHT) / 2, width, color);
        }
    }

    private static void drawMouseKey(Minecraft mc, int x, int y, int width, int height,
                                     String label, float active, int cps) {
        int background = blend(backgroundColor, pressedBackgroundColor, active);
        Gui.drawRect(x, y, x + width, y + height, background);
        if (border) {
            int t = Math.max(1, Math.round(borderThickness));
            Gui.drawRect(x, y, x + width, y + t, borderColor);
            Gui.drawRect(x, y + height - t, x + width, y + height, borderColor);
            Gui.drawRect(x, y, x + t, y + height, borderColor);
            Gui.drawRect(x + width - t, y, x + width, y + height, borderColor);
        }
        int color = active > 0.01F ? pressedTextColor : textColor;
        drawScaledCentered(mc, label, x, y + 1, width, color, 0.78F);
        if (cps >= 0) drawScaledCentered(mc, cps + " CPS", x, y + height - 6, width, color, 0.58F);
    }

    private static void drawScaledCentered(Minecraft mc, String text, int x, int y,
                                           int width, int color, float factor) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + width / 2.0F, y, 0.0F);
        GlStateManager.scale(factor, factor, 1.0F);
        int tx = -mc.fontRendererObj.getStringWidth(text) / 2;
        if (textShadow) mc.fontRendererObj.drawStringWithShadow(text, tx, 0, color);
        else mc.fontRendererObj.drawString(text, tx, 0, color, false);
        GlStateManager.popMatrix();
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
        return Math.max(12, Math.round(size * 0.72F));
    }

    public static int getHudHeight() {
        load();
        int size = boxSize, gap = Math.max(1, size / 9);
        int height = 0;
        if (showMovementKeys) height += size * 2 + gap;
        if (showClicks) height += getMouseHeight(size) + (height > 0 ? gap : 0);
        if (showSpaceBar) height += Math.max(4, size / 2) + (height > 0 ? gap : 0);
        return height;
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
    public static int getBackgroundColor() { load(); return backgroundColor; }
    public static int getPressedBackgroundColor() { load(); return pressedBackgroundColor; }
    public static int getTextColor() { load(); return textColor; }
    public static int getPressedTextColor() { load(); return pressedTextColor; }
    public static int getBorderColor() { load(); return borderColor; }
    public static void setColor(String key, int value) {
        load();
        if ("background".equals(key)) backgroundColor = value;
        else if ("pressedBackground".equals(key)) pressedBackgroundColor = value;
        else if ("text".equals(key)) textColor = value;
        else if ("pressedText".equals(key)) pressedTextColor = value;
        else if ("border".equals(key)) borderColor = value;
        save();
    }
    public static boolean isTextShadow() { load(); return textShadow; }
    public static void setTextShadow(boolean v) { load(); textShadow = v; save(); }
    public static int getFadeDelay() { load(); return fadeDelay; }
    public static void adjustFadeDelay(int delta) { load(); fadeDelay = clamp(fadeDelay + delta, 0, 500); save(); }
    public static int getBoxSize() { load(); return boxSize; }
    public static void adjustBoxSize(int delta) { load(); boxSize = clamp(boxSize + delta, 10, 32); save(); }
    public static float getBorderThickness() { load(); return borderThickness; }
    public static void adjustBorderThickness(float delta) { load(); borderThickness = Math.max(0.5F, Math.min(3.0F, borderThickness + delta)); save(); }
}
