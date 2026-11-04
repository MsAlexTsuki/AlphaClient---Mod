// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.quality.enchantingname;

import net.minecraft.client.Minecraft;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/** Stores the Enchanting Names feature toggle. */
public final class EnchantingNameMod {
    private static boolean enabled;

    private EnchantingNameMod() {
    }

    private static AlphaModuleFeature profileFeature() {
        return AlphaModuleRegistry.findFeature("enchanting-names");
    }

    public static boolean isEnabled() {
        AlphaModuleFeature feature = profileFeature();
        return feature != null ? feature.isEnabled() : enabled;
    }

    public static void setEnabled(boolean shouldEnable) {
        AlphaModuleFeature feature = profileFeature();
        if (feature != null) feature.setEnabled(shouldEnable);
        if (enabled == shouldEnable) return;
        enabled = shouldEnable;
        saveSettings();
    }

    public static void loadSettings() {
        FileReader reader = null;
        try {
            File file = getSettingsFile();
            if (!file.isFile()) {
                saveSettings();
                return;
            }

            reader = new FileReader(file);
            com.google.gson.JsonElement root =
                    new com.google.gson.JsonParser().parse(reader);
            if (root != null && root.isJsonObject()) {
                com.google.gson.JsonElement value = root.getAsJsonObject().get("enabled");
                if (value != null && value.isJsonPrimitive()) {
                    enabled = value.getAsBoolean();
                }
            }
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not load Enchanting Names settings: " + e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static void saveSettings() {
        File file = getSettingsFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;

        com.google.gson.JsonObject root = new com.google.gson.JsonObject();
        root.addProperty("enabled", enabled);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save Enchanting Names settings: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File getSettingsFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File base = minecraft == null ? new File(".") : minecraft.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-enchantingnames.json");
    }
}
