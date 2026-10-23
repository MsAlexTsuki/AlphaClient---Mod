package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;

import java.awt.Color;

/** Tracks elapsed time for the lifetime of the running Minecraft client. */
public final class SessionTimerHud {
    private static final String MODULE_ID = "sessionTimer";
    private static final String[] COLOR_NAMES = {
            "White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Rainbow"
    };
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55,
            0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA
    };
    private static final String[] COLOR_NAMES = {
            "White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Rainbow"
    };
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55,
            0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA
    };
    private static long sessionStartedAt;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Start once per Minecraft process, including time spent in menus or between worlds.
        // Leaving a server/world must not reset the session timer.
        if (sessionStartedAt <= 0L) {
            sessionStartedAt = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI
                || !isEnabled() || sessionStartedAt <= 0L) return;

        long elapsedSeconds = Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L);
        String text = "SESSION " + formatDuration(elapsedSeconds);
        int x = getHudX();
        int y = getHudY();
        renderHud(mc, text);
    }

    /** Draws a selectable preview in the HUD editor without changing the session clock. */
    public static void renderEditorPreview() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null) return;
        long elapsedSeconds = sessionStartedAt > 0L
                ? Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L) : 0L;
        renderHud(mc, "SESSION " + formatDuration(elapsedSeconds));
    }

    private static void renderHud(Minecraft mc, String text) {
        int x = getHudX();
        int y = getHudY();
        int textWidth = mc.fontRendererObj.getStringWidth(text) + 10;
        if (isShowBackground()) {
            Gui.drawRect(x - 3, y - 3, x + textWidth, y + mc.fontRendererObj.FONT_HEIGHT + 3, 0x990A0C11);
            Gui.drawRect(x - 3, y - 3, x + textWidth, y - 2, 0xFFFFD866);
        }
        mc.fontRendererObj.drawStringWithShadow(text, x, y, getTextColor());
    }

    public static String getColorName() {
        return COLOR_NAMES[getColorIndex()];
    }

    public static void cycleColor() {
        getSettings().addProperty("colorIndex", (getColorIndex() + 1) % COLOR_NAMES.length);
        ModuleProfileManager.save();
    }

    private static int getColorIndex() {
        JsonObject settings = getSettings();
        int index = settings.has("colorIndex") ? settings.get("colorIndex").getAsInt() : 3;
        return index < 0 || index >= COLOR_NAMES.length ? 3 : index;
    }

    private static int getTextColor() {
        int index = getColorIndex();
        if (index == COLOR_NAMES.length - 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
        }
        return COLORS[index];
    }

    public static String getColorName() {
        return COLOR_NAMES[getColorIndex()];
    }

    public static void cycleColor() {
        getSettings().addProperty("colorIndex", (getColorIndex() + 1) % COLOR_NAMES.length);
        ModuleProfileManager.save();
    }

    private static int getColorIndex() {
        JsonObject settings = getSettings();
        int index = settings.has("colorIndex") ? settings.get("colorIndex").getAsInt() : 3;
        return index < 0 || index >= COLOR_NAMES.length ? 3 : index;
    }

    private static int getTextColor() {
        int index = getColorIndex();
        if (index == COLOR_NAMES.length - 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0F;
            return 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
        }
        return COLORS[index];
    }

    public static boolean isShowBackground() {
        JsonObject settings = getSettings();
        return !settings.has("showBackground") || settings.get("showBackground").getAsBoolean();
    }

    public static void setShowBackground(boolean show) {
        getSettings().addProperty("showBackground", show);
        ModuleProfileManager.save();
    }

    public static boolean isEnabled() {
        JsonObject settings = getSettings();
        return settings.has("enabled") && settings.get("enabled").getAsBoolean();
    }

    public static void setEnabled(boolean enabled) {
        getSettings().addProperty("enabled", enabled);
        ModuleProfileManager.save();
    }

    public static int getHudX() {
        JsonObject settings = getSettings();
        return settings.has("hudX") ? Math.max(0, settings.get("hudX").getAsInt()) : 6;
    }

    public static int getHudY() {
        JsonObject settings = getSettings();
        return settings.has("hudY") ? Math.max(0, settings.get("hudY").getAsInt()) : 22;
    }

    public static void setHudPosition(int x, int y) {
        JsonObject settings = getSettings();
        settings.addProperty("hudX", Math.max(0, x));
        settings.addProperty("hudY", Math.max(0, y));
        ModuleProfileManager.save();
    }

    private static JsonObject getSettings() {
        return ModuleProfileManager.getSelectedModule(MODULE_ID);
    }

    private static String formatDuration(long totalSeconds) {
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (days > 0L) return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
