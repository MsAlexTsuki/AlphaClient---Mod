// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.config;

import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Persistent, bounded scale values shared by every HUD preview and live renderer. */
public final class HudScaleConfig {
    public static final float MIN_SCALE = 0.50F;
    public static final float MAX_SCALE = 2.00F;
    private static final Properties VALUES = new Properties();
    private static boolean loaded;
    private HudScaleConfig() { }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        File file = getFile();
        if (!file.isFile()) return;
        FileInputStream in = null;
        try { in = new FileInputStream(file); VALUES.load(in); }
        catch (Exception e) { System.err.println("[AlphaClient] Could not load HUD scales: " + e.getMessage()); }
        finally { if (in != null) try { in.close(); } catch (Exception ignored) { } }
    }

    private static File getFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient_hud_scales.properties");
    }

    public static synchronized float get(String id) {
        load();
        try { return clamp(Float.parseFloat(VALUES.getProperty(id, "1.0"))); }
        catch (Exception ignored) { return 1.0F; }
    }

    public static synchronized void set(String id, float scale) {
        load();
        VALUES.setProperty(id, Float.toString(clamp(scale)));
        File file = getFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        FileOutputStream out = null;
        try { out = new FileOutputStream(file); VALUES.store(out, "AlphaClient per-module HUD scales"); }
        catch (Exception e) { System.err.println("[AlphaClient] Could not save HUD scales: " + e.getMessage()); }
        finally { if (out != null) try { out.close(); } catch (Exception ignored) { } }
    }

    public static float clamp(float scale) { return Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale)); }
    public static void resetAll() {
        load();
        VALUES.clear();
        File file = getFile();
        if (file.exists()) file.delete();
    }
}