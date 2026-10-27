// --- Made By MsAlexTsuki ---
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
    private static final String KEY_POTION_PUNCH_ANIMATION = "potionPunchAnimation";
    private static final String KEY_FOOD_DRINK_ANIMATION = "foodDrinkAnimation";
    private static final String KEY_LEGACY_SWORD_SWING = "legacySwordSwing";
    private static final String KEY_LEGACY_ROD_ANIMATION = "legacyRodAnimation";
    private static boolean loaded;
    private static boolean disableViewBobbing = true;
    private static boolean enableBlockhit = true;
    private static boolean removeMissedSwingDelay = true;
    private static boolean legacyBowAnimation = true;
    private static boolean potionPunchAnimation = true;
    private static boolean foodDrinkAnimation = true;
    private static boolean legacySwordSwing = true;
    private static boolean legacyRodAnimation = true;

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
            if (root.has(KEY_POTION_PUNCH_ANIMATION)) {
                potionPunchAnimation = root.get(KEY_POTION_PUNCH_ANIMATION).getAsBoolean();
            }
            if (root.has(KEY_FOOD_DRINK_ANIMATION)) {
                foodDrinkAnimation = root.get(KEY_FOOD_DRINK_ANIMATION).getAsBoolean();
            }
            if (root.has(KEY_LEGACY_SWORD_SWING)) {
                legacySwordSwing = root.get(KEY_LEGACY_SWORD_SWING).getAsBoolean();
            }
            if (root.has(KEY_LEGACY_ROD_ANIMATION)) {
                legacyRodAnimation = root.get(KEY_LEGACY_ROD_ANIMATION).getAsBoolean();
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

    public static synchronized boolean isPotionPunchAnimationEnabled() {
        load();
        return potionPunchAnimation;
    }

    public static synchronized void setPotionPunchAnimationEnabled(boolean value) {
        load();
        potionPunchAnimation = value;
        save();
    }

    public static synchronized boolean isFoodDrinkAnimationEnabled() {
        load();
        return foodDrinkAnimation;
    }

    public static synchronized void setFoodDrinkAnimationEnabled(boolean value) {
        load();
        foodDrinkAnimation = value;
        save();
    }

    public static synchronized boolean isLegacySwordSwingEnabled() {
        load();
        return legacySwordSwing;
    }

    public static synchronized void setLegacySwordSwingEnabled(boolean value) {
        load();
        legacySwordSwing = value;
        save();
    }

    public static synchronized boolean isLegacyRodAnimationEnabled() {
        load();
        return legacyRodAnimation;
    }

    public static synchronized void setLegacyRodAnimationEnabled(boolean value) {
        load();
        legacyRodAnimation = value;
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
            root.addProperty(KEY_POTION_PUNCH_ANIMATION, potionPunchAnimation);
            root.addProperty(KEY_FOOD_DRINK_ANIMATION, foodDrinkAnimation);
            root.addProperty(KEY_LEGACY_SWORD_SWING, legacySwordSwing);
            root.addProperty(KEY_LEGACY_ROD_ANIMATION, legacyRodAnimation);
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
