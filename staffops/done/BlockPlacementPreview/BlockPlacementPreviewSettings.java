package com.alpharip.staffmod.BlockPlacementPreview;

import com.alpharip.staffmod.WayPoints.WaypointFileUtils;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Locally persisted options for the block placement preview. */
public final class BlockPlacementPreviewSettings {
    private static final Properties VALUES = new Properties();
    private static boolean loaded;
    private static boolean loadFailed;

    private BlockPlacementPreviewSettings() { }

    public static synchronized boolean isEnabled() { load(); return getBoolean("enabled", false); }
    public static synchronized boolean isShowCollision() { load(); return getBoolean("showCollision", true); }
    public static synchronized float getOpacity() {
        load();
        try { return clamp(Float.parseFloat(VALUES.getProperty("opacity", "0.5"))); }
        catch (NumberFormatException ignored) { return 0.5F; }
    }

    public static synchronized void setEnabled(boolean value) { load(); VALUES.setProperty("enabled", String.valueOf(value)); save(); }
    public static synchronized void setShowCollision(boolean value) { load(); VALUES.setProperty("showCollision", String.valueOf(value)); save(); }
    public static synchronized void setOpacity(float value) { load(); VALUES.setProperty("opacity", String.valueOf(clamp(value))); save(); }

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
        FileInputStream in = null;
        try {
            File source = file();
            if (source.isFile()) {
                in = new FileInputStream(source);
                VALUES.load(in);
            }
        } catch (Exception e) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load block preview settings; the file will be left unchanged: " + e.getMessage());
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) { }
        }
    }

    private static synchronized void save() {
        if (loadFailed) return;
        File destination = file();
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) return;
        File temporary = new File(parent, destination.getName() + ".tmp");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(temporary);
            VALUES.store(out, "AlphaClient block placement preview");
            out.close();
            out = null;
            WaypointFileUtils.replaceAtomically(temporary, destination);
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save block preview settings: " + e.getMessage());
        } finally {
            if (out != null) try { out.close(); } catch (Exception ignored) { }
        }
    }

    private static File file() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = new File(mc == null ? new File(".") : mc.mcDataDir, "config/alphaclient");
        return new File(base, "block-placement-preview.properties");
    }
}
