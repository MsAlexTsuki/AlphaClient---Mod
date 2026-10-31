package com.alpharip.staffmod.AutoTool;

import com.alpharip.staffmod.WayPoints.WaypointFileUtils;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Local settings for AutoTool. */
public final class AutoToolSettings {
    private static final Properties VALUES = new Properties();
    private static boolean loaded;
    private static boolean loadFailed;

    private AutoToolSettings() { }

    public static synchronized boolean isEnabled() {
        load();
        return getBoolean("enabled", false);
    }

    public static synchronized boolean isSwapBackEnabled() {
        load();
        return getBoolean("swapBack", false);
    }

    public static synchronized void setEnabled(boolean enabled) {
        load();
        VALUES.setProperty("enabled", String.valueOf(enabled));
        save();
    }

    public static synchronized void setSwapBackEnabled(boolean enabled) {
        load();
        VALUES.setProperty("swapBack", String.valueOf(enabled));
        save();
    }

    private static boolean getBoolean(String key, boolean fallback) {
        String value = VALUES.getProperty(key);
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        FileInputStream input = null;
        try {
            File source = file();
            if (source.isFile()) {
                input = new FileInputStream(source);
                VALUES.load(input);
            }
        } catch (Exception error) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load AutoTool settings; saving is disabled to protect the existing file: "
                    + error.getMessage());
        } finally {
            if (input != null) {
                try { input.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static void save() {
        if (loadFailed) return;
        File destination = file();
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) return;
        File temporary = new File(parent, destination.getName() + ".tmp");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(temporary);
            VALUES.store(output, "AlphaClient AutoTool settings");
            output.close();
            output = null;
            WaypointFileUtils.replaceAtomically(temporary, destination);
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not save AutoTool settings: " + error.getMessage());
        } finally {
            if (output != null) {
                try { output.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File file() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = new File(mc == null ? new File(".") : mc.mcDataDir, "config/alphaclient");
        return new File(base, "autotool.properties");
    }
}
