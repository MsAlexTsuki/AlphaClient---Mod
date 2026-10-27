// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.config;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.combat.AimAssistSettings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/** Persists module enable states to config/alphaclient.json. */
public final class AlphaClientConfig {
    private static boolean loading = false;

    private AlphaClientConfig() {
    }

    public static synchronized void load() {
        File file = getConfigFile();
        if (!file.isFile()) {
            save();
            return;
        }
        loading = true;
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            JsonElement root = new JsonParser().parse(reader);
            if (root != null && root.isJsonObject()) {
                JsonObject rootObject = root.getAsJsonObject();
                JsonObject modules = rootObject.getAsJsonObject("modules");
                JsonObject aimAssist = rootObject.getAsJsonObject("aimAssist");
                if (aimAssist != null) {
                    AimAssistSettings.loadFromJson(aimAssist);
                }
                if (modules != null) {
                    for (AlphaModule module : AlphaModuleRegistry.getAll()) {
                        JsonElement value = modules.get(module.getId());
                        if (value != null && value.isJsonPrimitive()) {
                            try {
                                module.restoreEnabledState(value.getAsBoolean());
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not load module config: " + e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) { }
            }
            loading = false;
        }
        save();
    }

    public static synchronized void saveModuleState(String moduleId, boolean enabled) {
        if (!loading) save();
    }

    public static synchronized void save() {
        File file = getConfigFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.out.println("[AlphaClient] Could not create config directory.");
            return;
        }
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            modules.addProperty(module.getId(), module.isConfiguredEnabled());
        }
        root.add("modules", modules);
        root.add("aimAssist", AimAssistSettings.toJson());
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save module config: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File getConfigFile() {
        Minecraft mc = FMLClientHandler.instance().getClient();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient.json");
    }
}
