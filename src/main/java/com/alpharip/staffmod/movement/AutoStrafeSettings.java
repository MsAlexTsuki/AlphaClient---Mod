// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.config.AlphaClientConfig;
import com.google.gson.JsonObject;

/** Persistent, validated settings for the Founder-only AutoStrafe module. */
public final class AutoStrafeSettings {
    private static final double DEFAULT_TARGET_RANGE = 6.0D;

    private static double targetRange = DEFAULT_TARGET_RANGE;

    private AutoStrafeSettings() { }

    public static synchronized double getTargetRange() { return targetRange; }

    public static synchronized void setTargetRange(double value) {
        targetRange = clamp(value, 3.0D, 8.0D);
        save();
    }

    public static synchronized void reset() {
        targetRange = DEFAULT_TARGET_RANGE;
        save();
    }

    public static synchronized JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("targetRange", targetRange);
        return json;
    }

    public static synchronized void loadFromJson(JsonObject json) {
        if (json == null) return;
        targetRange = readDouble(json, "targetRange", DEFAULT_TARGET_RANGE, 3.0D, 8.0D);
    }

    private static void save() { AlphaClientConfig.save(); }

    private static double readDouble(JsonObject json, String key, double fallback, double min, double max) {
        try {
            return json.has(key) ? clamp(json.get(key).getAsDouble(), min, max) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
