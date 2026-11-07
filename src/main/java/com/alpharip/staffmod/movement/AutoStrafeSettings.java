// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.config.AlphaClientConfig;
import com.google.gson.JsonObject;

/** Persistent, validated settings for the Founder-only AutoStrafe module. */
public final class AutoStrafeSettings {
    public enum Direction {
        LEFT, RIGHT
    }

    private static final int DEFAULT_SPEED = 75;
    private static final double DEFAULT_ORBIT_DISTANCE = 2.5D;
    private static final double DEFAULT_TARGET_RANGE = 6.0D;

    private static int speed = DEFAULT_SPEED;
    private static double orbitDistance = DEFAULT_ORBIT_DISTANCE;
    private static double targetRange = DEFAULT_TARGET_RANGE;
    private static Direction direction = Direction.LEFT;

    private AutoStrafeSettings() { }

    public static synchronized int getSpeed() { return speed; }
    public static synchronized double getOrbitDistance() { return orbitDistance; }
    public static synchronized double getTargetRange() { return targetRange; }
    public static synchronized Direction getDirection() { return direction; }

    public static synchronized void setSpeed(int value) {
        speed = clamp(value, 25, 100);
        save();
    }

    public static synchronized void setOrbitDistance(double value) {
        orbitDistance = clamp(value, 1.5D, 4.0D);
        save();
    }

    public static synchronized void setTargetRange(double value) {
        targetRange = clamp(value, 3.0D, 8.0D);
        save();
    }

    public static synchronized void cycleDirection() {
        direction = direction == Direction.LEFT ? Direction.RIGHT : Direction.LEFT;
        save();
    }

    public static synchronized void reset() {
        speed = DEFAULT_SPEED;
        orbitDistance = DEFAULT_ORBIT_DISTANCE;
        targetRange = DEFAULT_TARGET_RANGE;
        direction = Direction.LEFT;
        save();
    }

    public static synchronized JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("speed", speed);
        json.addProperty("orbitDistance", orbitDistance);
        json.addProperty("targetRange", targetRange);
        json.addProperty("direction", direction.name());
        return json;
    }

    public static synchronized void loadFromJson(JsonObject json) {
        if (json == null) return;
        speed = readInt(json, "speed", DEFAULT_SPEED, 25, 100);
        orbitDistance = readDouble(json, "orbitDistance", DEFAULT_ORBIT_DISTANCE, 1.5D, 4.0D);
        targetRange = readDouble(json, "targetRange", DEFAULT_TARGET_RANGE, 3.0D, 8.0D);
        try {
            direction = json.has("direction")
                    ? Direction.valueOf(json.get("direction").getAsString()) : Direction.LEFT;
        } catch (Exception ignored) {
            direction = Direction.LEFT;
        }
    }

    private static void save() { AlphaClientConfig.save(); }

    private static int readInt(JsonObject json, String key, int fallback, int min, int max) {
        try {
            return json.has(key) ? clamp(json.get(key).getAsInt(), min, max) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static double readDouble(JsonObject json, String key, double fallback, double min, double max) {
        try {
            return json.has(key) ? clamp(json.get(key).getAsDouble(), min, max) : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
