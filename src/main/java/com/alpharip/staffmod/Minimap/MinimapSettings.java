package com.alpharip.staffmod.Minimap;

import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Persistent presentation and keybind settings for the AlphaClient minimap. */
public final class MinimapSettings {
    public static final String TOP_LEFT = "top-left";
    public static final String TOP_RIGHT = "top-right";
    public static final String BOTTOM_LEFT = "bottom-left";
    public static final String BOTTOM_RIGHT = "bottom-right";
    private static final Properties VALUES = new Properties();
    private static boolean loaded;
    private static boolean loadFailed;

    private MinimapSettings() { }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        FileInputStream in = null;
        try {
            File file = file();
            if (file.isFile()) {
                in = new FileInputStream(file);
                VALUES.load(in);
            }
        } catch (Exception e) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load minimap settings: " + e.getMessage());
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) { }
        }
    }

    private static File file() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = new File(mc == null ? new File(".") : mc.mcDataDir, "config/alphaclient");
        if (!base.exists()) base.mkdirs();
        return new File(base, "minimap.properties");
    }

    public static synchronized int getInt(String key, int fallback, int min, int max) {
        load();
        try { return Math.max(min, Math.min(max, Integer.parseInt(VALUES.getProperty(key, String.valueOf(fallback))))); }
        catch (Exception ignored) { return fallback; }
    }

    public static synchronized String getString(String key, String fallback) {
        load();
        String value = VALUES.getProperty(key, fallback);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public static synchronized void setInt(String key, int value) {
        load(); VALUES.setProperty(key, String.valueOf(value)); save();
    }

    public static synchronized void setString(String key, String value) {
        load();
        if (value == null) VALUES.remove(key); else VALUES.setProperty(key, value);
        save();
    }

    private static void save() {
        if (loadFailed) return;
        File destination = file();
        File temporary = new File(destination.getParentFile(), destination.getName() + ".tmp");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(temporary);
            VALUES.store(out, "AlphaClient minimap settings");
            out.close(); out = null;
            if (destination.exists() && !destination.delete()) throw new IllegalStateException("Could not replace minimap settings.");
            if (!temporary.renameTo(destination)) throw new IllegalStateException("Could not save minimap settings.");
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save minimap settings: " + e.getMessage());
        } finally {
            if (out != null) try { out.close(); } catch (Exception ignored) { }
        }
    }

    public static int size() { return getInt("size", 112, 80, 180); }
    public static int offset() { return getInt("offset", 8, 0, 40); }
    public static int range() { return getInt("range", 96, 32, 256); }
    public static boolean round() { return "round".equals(getString("shape", "square")); }
    public static String corner() { return getString("corner", TOP_RIGHT); }
    public static int coordinateColor() { return getInt("coordinateColor", 0xFFFFD866, 0, 0xFFFFFF); }
}
