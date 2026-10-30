// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import net.minecraft.client.Minecraft;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/** Global, persistent kill switch for every Pixel Garden entry point and background task. */
public final class PixelGardenModuleSettings {
    private static final PixelGardenModuleSettings INSTANCE = new PixelGardenModuleSettings();
    private volatile boolean enabled = true;
    private boolean loaded;

    private PixelGardenModuleSettings() { }

    public static PixelGardenModuleSettings getInstance() { return INSTANCE; }

    public synchronized boolean isEnabled() {
        ensureLoaded();
        return enabled;
    }

    public synchronized void setEnabled(boolean value) {
        ensureLoaded();
        enabled = value;
        save();
    }

    private void ensureLoaded() {
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
            if (json.has("enabled")) enabled = json.get("enabled").getAsBoolean();
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not load Pixel Garden module setting: " + e.getMessage());
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception ignored) { }
        }
    }

    private void save() {
        File file = getFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        FileWriter writer = null;
        try {
            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            writer = new FileWriter(file);
            writer.write(json.toString());
            writer.flush();
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save Pixel Garden module setting: " + e.getMessage());
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception ignored) { }
        }
    }

    private File getFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-pixelgarden.json");
    }
}
