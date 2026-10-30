// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldProvider;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/** Owns the AlphaClient controls that override Minecraft's cloud settings. */
public final class CloudHeightSettings {
    private static boolean cloudsEnabled = true;
    private static int cloudStyle = 2;
    private static float cloudHeight = 128.0F;
    private static boolean loaded;

    private CloudHeightSettings() { }

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        File file = getFile();
        if (!file.isFile()) {
            save();
            return;
        }
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            if (json.has("cloudsEnabled")) cloudsEnabled = json.get("cloudsEnabled").getAsBoolean();
            if (json.has("cloudStyle")) cloudStyle = clamp(json.get("cloudStyle").getAsInt(), 1, 2);
            if (json.has("cloudHeight")) cloudHeight = clamp(json.get("cloudHeight").getAsFloat(), 0.0F,  cloudsMaxHeight());
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not load Cloud Height settings: " + error.getMessage());
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception ignored) { }
        }
        apply();
    }

    public static synchronized void save() {
        File file = getFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        JsonObject json = new JsonObject();
        json.addProperty("cloudsEnabled", cloudsEnabled);
        json.addProperty("cloudStyle", cloudStyle);
        json.addProperty("cloudHeight", cloudHeight);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(json.toString());
            writer.flush();
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not save Cloud Height settings: " + error.getMessage());
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception ignored) { }
        }
    }

    public static synchronized void apply() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.gameSettings != null) {
            mc.gameSettings.clouds = cloudsEnabled ? cloudStyle : 0;
        }
    }

    public static synchronized float overrideCloudHeight(WorldProvider provider) {
        return cloudHeight;
    }

    public static synchronized boolean isCloudsEnabled() { return cloudsEnabled; }
    public static synchronized int getCloudStyle() { return cloudStyle; }
    public static synchronized float getCloudHeight() { return cloudHeight; }

    public static synchronized void toggleClouds() {
        cloudsEnabled = !cloudsEnabled;
        apply();
        save();
    }

    public static synchronized void cycleCloudStyle() {
        cloudStyle = cloudStyle == 1 ? 2 : 1;
        apply();
        save();
    }

    public static synchronized void changeCloudHeight(float amount) {
        cloudHeight = clamp(cloudHeight + amount, 0.0F, cloudsMaxHeight());
        save();
    }

    private static float cloudsMaxHeight() { return 256.0F; }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    private static File getFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-cloud-height.json");
    }
}
