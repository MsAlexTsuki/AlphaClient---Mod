package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/** Tracks cumulative Minecraft process runtime independently of the HUD toggle. */
public final class PlaytimeTrackerHud {
    private static final String MODULE_ID = "playtimeTracker";
    private static final long SAVE_INTERVAL_MS = 10_000L;
    private static final long SECOND = 1L;
    private static final long MINUTE = 60L * SECOND;
    private static final long HOUR = 60L * MINUTE;
    private static final long DAY = 24L * HOUR;
    private static final long MONTH = 30L * DAY;
    private static final long YEAR = 365L * DAY;
    private static final String[] COLOR_NAMES = {
            "White", "Red", "Orange", "Yellow", "Green", "Cyan", "Blue", "Purple", "Pink", "Rainbow"
    };
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55,
            0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA
    };

    private static long savedPlaytimeSeconds;
    private static long sessionStartedAt;
    private static long lastSaveAt;
    private static boolean initialized;
    private static boolean shutdownHookRegistered;

    /** Loads the saved total and starts counting even when the HUD is disabled. */
    public static synchronized void initialize() {
        if (initialized) return;
        savedPlaytimeSeconds = loadPlaytime();
        sessionStartedAt = System.currentTimeMillis();
        lastSaveAt = sessionStartedAt;
        initialized = true;
        if (!shutdownHookRegistered) {
            shutdownHookRegistered = true;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    savePlaytime();
                }
            }, "AlphaClient-Playtime-Save"));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!initialized) initialize();
        long now = System.currentTimeMillis();
        if (now - lastSaveAt >= SAVE_INTERVAL_MS) {
            savePlaytime();
            lastSaveAt = now;
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI
                || !isEnabled()) return;
        if (!initialized) initialize();
        renderHud(mc, "PLAYTIME " + formatDuration(getTotalPlaytimeSeconds()));
    }

    /** Draws a draggable HUD preview without affecting the stored playtime. */
    public static void renderEditorPreview() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null) return;
        if (!initialized) initialize();
        renderHud(mc, "PLAYTIME " + formatDuration(getTotalPlaytimeSeconds()));
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

    public static synchronized long getTotalPlaytimeSeconds() {
        if (!initialized) initialize();
        long elapsed = Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L);
        return savedPlaytimeSeconds + elapsed;
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
        return settings.has("hudY") ? Math.max(0, settings.get("hudY").getAsInt()) : 38;
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

    private static synchronized long loadPlaytime() {
        File file = getPlaytimeFile();
        if (!file.isFile()) return 0L;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String value = reader.readLine();
            return value == null ? 0L : Math.max(0L, Long.parseLong(value.trim()));
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not load Playtime Tracker data: " + e.getMessage());
            return 0L;
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static synchronized void savePlaytime() {
        if (!initialized) return;
        File file = getPlaytimeFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.out.println("[AlphaClient] Could not create Playtime Tracker data directory.");
            return;
        }
        long total = savedPlaytimeSeconds
                + Math.max(0L, (System.currentTimeMillis() - sessionStartedAt) / 1000L);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(Long.toString(total));
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save Playtime Tracker data: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File getPlaytimeFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-playtime.txt");
    }

    /** Formats elapsed playtime using at most two non-zero units and English plurals. */
    public static String formatDuration(long totalSeconds) {
        long remaining = Math.max(0L, totalSeconds);
        long[] sizes = { YEAR, MONTH, DAY, HOUR, MINUTE, SECOND };
        String[] singular = { "year", "month", "day", "hour", "minute", "second" };
        StringBuilder result = new StringBuilder();
        int unitsAdded = 0;
        for (int i = 0; i < sizes.length && unitsAdded < 2; i++) {
            long amount = remaining / sizes[i];
            if (amount <= 0L) continue;
            remaining %= sizes[i];
            if (unitsAdded > 0) result.append(" and ");
            result.append(amount).append(' ').append(singular[i]);
            if (amount != 1L) result.append('s');
            unitsAdded++;
        }
        return result.length() == 0 ? "0 seconds" : result.toString();
    }
}
