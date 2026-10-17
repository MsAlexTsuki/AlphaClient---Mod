package com.alpharip.staffmod.pvp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/** Persisted user-configurable options for Legacy PvP. */
public final class LegacyPvpSettings {
    private static final String KEY_DISABLE_VIEW_BOBBING = "disableViewBobbing";
    private static final String KEY_ENABLE_BLOCKHIT = "enableBlockhit";
    private static final String KEY_REMOVE_MISSED_SWING_DELAY = "removeMissedSwingDelay";
    private static final String KEY_LEGACY_BOW_ANIMATION = "legacyBowAnimation";
    private static boolean loaded;
    private static boolean disableViewBobbing = true;
    private static boolean enableBlockhit = true;
    private static boolean removeMissedSwingDelay = true;
    private static boolean legacyBowAnimation = true;

    private LegacyPvpSettings() { }

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        FileReader reader = null;
        try {
            File file = getFile();
            if (!file.isFile()) {
                save();
                return;
            }
            reader = new FileReader(file);
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            if (root.has(KEY_DISABLE_VIEW_BOBBING)) {
                disableViewBobbing = root.get(KEY_DISABLE_VIEW_BOBBING).getAsBoolean();
            }
            if (root.has(KEY_ENABLE_BLOCKHIT)) {
                enableBlockhit = root.get(KEY_ENABLE_BLOCKHIT).getAsBoolean();
            }
            if (root.has(KEY_REMOVE_MISSED_SWING_DELAY)) {
                removeMissedSwingDelay = root.get(KEY_REMOVE_MISSED_SWING_DELAY).getAsBoolean();
            }
            if (root.has(KEY_LEGACY_BOW_ANIMATION)) {
                legacyBowAnimation = root.get(KEY_LEGACY_BOW_ANIMATION).getAsBoolean();
            }
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not load Legacy PvP settings: " + e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) { }
            }
        }
    }

    public static synchronized boolean isDisableViewBobbing() {
        load();
        return disableViewBobbing;
    }

    public static synchronized void setDisableViewBobbing(boolean value) {
        load();
        disableViewBobbing = value;
        save();
    }

    public static synchronized boolean isBlockhitEnabled() {
        load();
        return enableBlockhit;
    }

    public static synchronized void setBlockhitEnabled(boolean value) {
        load();
        enableBlockhit = value;
        save();
    }

    public static synchronized boolean isRemoveMissedSwingDelayEnabled() {
        load();
        return removeMissedSwingDelay;
    }

    public static synchronized void setRemoveMissedSwingDelayEnabled(boolean value) {
        load();
        removeMissedSwingDelay = value;
        save();
    }

    public static synchronized boolean isLegacyBowAnimationEnabled() {
        load();
        return legacyBowAnimation;
    }

    public static synchronized void setLegacyBowAnimationEnabled(boolean value) {
        load();
        legacyBowAnimation = value;
        save();
    }

    public static synchronized void save() {
        FileWriter writer = null;
        try {
            File file = getFile();
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) return;
            JsonObject root = new JsonObject();
            root.addProperty(KEY_DISABLE_VIEW_BOBBING, disableViewBobbing);
            root.addProperty(KEY_ENABLE_BLOCKHIT, enableBlockhit);
            root.addProperty(KEY_REMOVE_MISSED_SWING_DELAY, removeMissedSwingDelay);
            root.addProperty(KEY_LEGACY_BOW_ANIMATION, legacyBowAnimation);
            writer = new FileWriter(file);
            writer.write(root.toString());
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save Legacy PvP settings: " + e.getMessage());
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File getFile() {
        Minecraft mc = FMLClientHandler.instance().getClient();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(new File(base, "config"), "alphaclient"), "legacy-pvp.json");
    }
}
