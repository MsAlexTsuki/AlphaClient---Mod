package com.alpharip.staffmod.singleplayer.autotool;

import com.alpharip.staffmod.waypoints.WaypointFileUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/** Local global defaults and per-server/world AutoTool overrides. */
public final class AutoToolSettings {
    private static final Properties GLOBAL = new Properties();
    private static Properties PROFILE = new Properties();
    private static boolean globalLoaded;
    private static boolean globalLoadFailed;
    private static boolean profileLoadFailed;
    private static String loadedProfileKey;
    private static String loadedProfileIdentity;

    private AutoToolSettings() { }

    public static synchronized boolean isEnabled() { return getBoolean("enabled", false); }

    public static synchronized String getSwapBackMode() {
        String mode = get("swapBackMode", null);
        if (mode == null) return getBoolean("swapBack", false) ? "RELEASE" : "NEVER";
        mode = mode.toUpperCase(Locale.ROOT);
        return "RELEASE".equals(mode) || "BROKEN".equals(mode) ? mode : "NEVER";
    }

    public static synchronized int getSwapBackDelay() { return clamp(getInt("swapBackDelay", 4), 0, 100); }
    public static synchronized int getMinimumDurability() { return clamp(getInt("minimumDurability", 0), 0, 100); }
    public static synchronized boolean isEfficiencyAware() { return getBoolean("efficiencyAware", true); }
    public static synchronized boolean isDurabilityTieBreakEnabled() { return getBoolean("durabilityTieBreak", true); }
    public static synchronized boolean isFeedbackEnabled() { return getBoolean("feedback", false); }
    public static synchronized boolean isPauseToggleMode() { return getBoolean("pauseToggleMode", false); }
    public static synchronized boolean isPauseWhenUsingItem() { return getBoolean("pauseWhenUsingItem", false); }
    public static synchronized boolean isPauseWhenSneaking() { return getBoolean("pauseWhenSneaking", false); }
    public static synchronized boolean isPauseInSurvival() { return getBoolean("pauseInSurvival", false); }
    public static synchronized boolean isPauseInCreative() { return getBoolean("pauseInCreative", false); }
    public static synchronized boolean isPauseInAdventure() { return getBoolean("pauseInAdventure", false); }

    public static synchronized String getPreferredItem(String blockId) {
        return get("prefer." + normalizeId(blockId), null);
    }

    public static synchronized void setPreferredItem(String blockId, String itemId) {
        String key = "prefer." + normalizeId(blockId);
        if (itemId == null || itemId.trim().isEmpty()) remove(key);
        else set(key, normalizeId(itemId));
    }

    public static synchronized boolean isItemIgnored(String itemId) {
        return getList("ignoredItems").contains(normalizeId(itemId));
    }

    public static synchronized void setItemIgnored(String itemId, boolean ignored) {
        String normalized = normalizeId(itemId);
        List<String> items = getList("ignoredItems");
        items.remove(normalized);
        if (ignored) items.add(normalized);
        set("ignoredItems", join(items));
    }

    public static synchronized String getProfileDescription() {
        ensureLoaded();
        return loadedProfileKey == null ? "Global defaults" : "Local profile for this server/world";
    }

    public static synchronized void setEnabled(boolean value) { set("enabled", String.valueOf(value)); }
    public static synchronized void setSwapBackMode(String value) {
        String mode = value == null ? "NEVER" : value.toUpperCase(Locale.ROOT);
        if (!"NEVER".equals(mode) && !"RELEASE".equals(mode) && !"BROKEN".equals(mode)) mode = "NEVER";
        set("swapBackMode", mode);
        set("swapBack", String.valueOf(!"NEVER".equals(mode)));
    }
    public static synchronized void setSwapBackDelay(int ticks) { set("swapBackDelay", String.valueOf(clamp(ticks, 0, 100))); }
    public static synchronized void setMinimumDurability(int percent) { set("minimumDurability", String.valueOf(clamp(percent, 0, 100))); }
    public static synchronized void setEfficiencyAware(boolean value) { set("efficiencyAware", String.valueOf(value)); }
    public static synchronized void setDurabilityTieBreakEnabled(boolean value) { set("durabilityTieBreak", String.valueOf(value)); }
    public static synchronized void setFeedbackEnabled(boolean value) { set("feedback", String.valueOf(value)); }
    public static synchronized void setPauseToggleMode(boolean value) { set("pauseToggleMode", String.valueOf(value)); }
    public static synchronized void setPauseWhenUsingItem(boolean value) { set("pauseWhenUsingItem", String.valueOf(value)); }
    public static synchronized void setPauseWhenSneaking(boolean value) { set("pauseWhenSneaking", String.valueOf(value)); }
    public static synchronized void setPauseInSurvival(boolean value) { set("pauseInSurvival", String.valueOf(value)); }
    public static synchronized void setPauseInCreative(boolean value) { set("pauseInCreative", String.valueOf(value)); }
    public static synchronized void setPauseInAdventure(boolean value) { set("pauseInAdventure", String.valueOf(value)); }

    private static String get(String key, String fallback) {
        ensureLoaded();
        String value = PROFILE.getProperty(key);
        if (value == null) value = GLOBAL.getProperty(key);
        return value == null ? fallback : value;
    }

    private static boolean getBoolean(String key, boolean fallback) {
        String value = get(key, null);
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

    private static int getInt(String key, int fallback) {
        try { return Integer.parseInt(get(key, String.valueOf(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static List<String> getList(String key) {
        List<String> values = new ArrayList<String>();
        String raw = get(key, "");
        for (String item : raw.split(",")) {
            String value = normalizeId(item);
            if (!value.isEmpty() && !values.contains(value)) values.add(value);
        }
        return values;
    }

    private static void set(String key, String value) {
        ensureLoaded();
        Properties target = loadedProfileKey == null ? GLOBAL : PROFILE;
        target.setProperty(key, value);
        save(loadedProfileKey == null ? globalFile() : profileFile(loadedProfileKey),
                loadedProfileKey == null ? globalLoadFailed : profileLoadFailed);
    }

    private static void remove(String key) {
        ensureLoaded();
        Properties target = loadedProfileKey == null ? GLOBAL : PROFILE;
        target.remove(key);
        save(loadedProfileKey == null ? globalFile() : profileFile(loadedProfileKey),
                loadedProfileKey == null ? globalLoadFailed : profileLoadFailed);
    }

    private static void ensureLoaded() {
        if (!globalLoaded) {
            globalLoaded = true;
            File file = globalFile();
            if (!file.isFile()) {
                // Keep the original AutoTool settings file as the global profile.
                File legacy = legacyFile();
                if (legacy.isFile()) loadInto(GLOBAL, legacy, true);
            } else {
                loadInto(GLOBAL, file, true);
            }
        }

        String identity = profileIdentity();
        if (identity == null ? loadedProfileIdentity != null : !identity.equals(loadedProfileIdentity)) {
            loadedProfileIdentity = identity;
            String currentKey = identity == null ? null : hashProfileIdentity(identity);
            loadedProfileKey = currentKey;
            PROFILE = new Properties();
            profileLoadFailed = false;
            if (currentKey != null) loadInto(PROFILE, profileFile(currentKey), false);
        }
    }

    private static void loadInto(Properties properties, File file, boolean globalFile) {
        if (!file.isFile()) return;
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
        } catch (Exception error) {
            if (globalFile) globalLoadFailed = true;
            else profileLoadFailed = true;
            System.err.println("[AlphaClient] Could not load AutoTool settings; protecting the existing file: "
                    + error.getMessage());
        } finally {
            if (input != null) try { input.close(); } catch (Exception ignored) { }
        }
    }

    private static void save(File destination, boolean loadFailed) {
        if (loadFailed) return;
        File parent = destination.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs() && !parent.isDirectory())) return;
        File temporary = new File(parent, destination.getName() + ".tmp");
        FileOutputStream output = null;
        try {
            Properties properties = destination.equals(globalFile()) ? GLOBAL : PROFILE;
            output = new FileOutputStream(temporary);
            properties.store(output, "AlphaClient AutoTool settings");
            output.close();
            output = null;
            WaypointFileUtils.replaceAtomically(temporary, destination);
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not save AutoTool settings: " + error.getMessage());
        } finally {
            if (output != null) try { output.close(); } catch (Exception ignored) { }
        }
    }

    private static String profileIdentity() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || !mc.isSingleplayer()
                || mc.getIntegratedServer() == null) return null;
        return "world:" + mc.getIntegratedServer().getFolderName();
    }

    private static String hashProfileIdentity(String identity) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(identity.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte value : digest) hex.append(String.format("%02x", value & 0xff));
            return hex.toString();
        } catch (Exception ignored) {
            return Integer.toHexString(identity.hashCode());
        }
    }

    private static File configDirectory() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = new File(mc == null ? new File(".") : mc.mcDataDir, "config/alphaclient");
        return base;
    }

    private static File globalFile() { return new File(configDirectory(), "autotool.properties"); }
    private static File legacyFile() { return globalFile(); }
    private static File profileFile(String key) { return new File(new File(configDirectory(), "autotool-profiles"), key + ".properties"); }

    private static String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private static String join(List<String> values) {
        StringBuilder output = new StringBuilder();
        for (String value : values) {
            if (output.length() > 0) output.append(',');
            output.append(value);
        }
        return output.toString();
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
