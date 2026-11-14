package com.alpharip.staffmod.BlockPlacementPreview;

import com.alpharip.staffmod.waypoints.WaypointFileUtils;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Local settings for the block placement preview. */
public final class BlockPlacementPreviewSettings {
    private static final Properties VALUES = new Properties();
    private static boolean loaded;
    private static boolean loadFailed;

    private BlockPlacementPreviewSettings() {
    }

    public static synchronized boolean isEnabled() {
        load();
        return getBoolean("enabled", false);
    }

    public static synchronized boolean isShowCollision() {
        load();
        return getBoolean("showCollision", true);
    }

    public static synchronized float getOpacity() {
        load();
        try {
            return clamp(Float.parseFloat(VALUES.getProperty("opacity", "0.5")));
        } catch (NumberFormatException ignored) {
            return 0.5F;
        }
    }

    public static synchronized void setEnabled(boolean enabled) {
        load();
        VALUES.setProperty("enabled", String.valueOf(enabled));
        save();
    }

    public static synchronized void setShowCollision(boolean showCollision) {
        load();
        VALUES.setProperty("showCollision", String.valueOf(showCollision));
        save();
    }

    public static synchronized void setOpacity(float opacity) {
        load();
        VALUES.setProperty("opacity", String.valueOf(clamp(opacity)));
        save();
    }

    private static boolean getBoolean(String key, boolean fallback) {
        String value = VALUES.getProperty(key);
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

    private static float clamp(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return 0.5F;
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;

        FileInputStream input = null;
        try {
            File source = getSettingsFile();
            if (source.isFile()) {
                input = new FileInputStream(source);
                VALUES.load(input);
            }
        } catch (Exception exception) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load block placement preview settings: "
                    + exception.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static synchronized void save() {
        if (loadFailed) return;

        File destination = getSettingsFile();
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            return;
        }

        File temporary = new File(parent, destination.getName() + ".tmp");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(temporary);
            VALUES.store(output, "AlphaClient block placement preview settings");
            output.close();
            output = null;
            WaypointFileUtils.replaceAtomically(temporary, destination);
        } catch (Exception exception) {
            System.err.println("[AlphaClient] Could not save block placement preview settings: "
                    + exception.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static File getSettingsFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File gameDirectory = minecraft == null ? new File(".") : minecraft.mcDataDir;
        return new File(gameDirectory, "config/alphaclient/block-placement-preview.properties");
    }
}
