// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.config.AlphaClientConfig;
import com.google.gson.JsonObject;

/** Persistent, validated configuration for the Founder-only AimAssist module. */
public final class AimAssistSettings {
    public enum Mode {
        SMOOTH, SNAP, DYNAMIC, PREDICTIVE, HORIZONTAL, VERTICAL, ADAPTIVE
    }

    public enum TargetPriority {
        CROSSHAIR, LOWEST_ANGLE, CLOSEST
    }

    public enum TargetArea {
        FULL_BODY("Full Body", new double[] {0.10D, 0.30D, 0.50D, 0.70D, 0.90D}),
        UPPER_BODY("Upper Body", new double[] {0.55D, 0.70D, 0.85D, 0.95D}),
        TORSO("Torso", new double[] {0.38D, 0.48D, 0.58D, 0.68D}),
        HEAD("Head", new double[] {0.86D, 0.93D, 0.98D});

        private final String label;
        private final double[] heightSamples;

        TargetArea(String label, double[] heightSamples) {
            this.label = label;
            this.heightSamples = heightSamples;
        }

        public String getLabel() { return label; }
        public double[] getHeightSamples() { return heightSamples.clone(); }
    }

    private static final int DEFAULT_CORRECTION_SPEED = 10;
    private static final int DEFAULT_REACTION_DELAY_MS = 0;
    private static final int DEFAULT_MAX_ANGLE_DEGREES = 5;
    private static final int DEFAULT_HORIZONTAL_SPEED = 10;
    private static final int DEFAULT_VERTICAL_SPEED = 10;
    private static final float DEFAULT_PREDICTION_TICKS = 2.0F;

    private static int correctionSpeed = DEFAULT_CORRECTION_SPEED;
    private static int reactionDelayMs = DEFAULT_REACTION_DELAY_MS;
    private static int maximumAngleDegrees = DEFAULT_MAX_ANGLE_DEGREES;
    private static int horizontalSpeed = DEFAULT_HORIZONTAL_SPEED;
    private static int verticalSpeed = DEFAULT_VERTICAL_SPEED;
    private static float predictionTicks = DEFAULT_PREDICTION_TICKS;
    private static Mode mode = Mode.SMOOTH;
    private static TargetPriority targetPriority = TargetPriority.CROSSHAIR;
    private static TargetArea targetArea = TargetArea.FULL_BODY;

    private AimAssistSettings() { }

    public static synchronized int getCorrectionSpeed() { return correctionSpeed; }
    public static synchronized int getReactionDelayMs() { return reactionDelayMs; }
    public static synchronized int getMaximumAngleDegrees() { return maximumAngleDegrees; }
    public static synchronized int getHorizontalSpeed() { return horizontalSpeed; }
    public static synchronized int getVerticalSpeed() { return verticalSpeed; }
    public static synchronized float getPredictionTicks() { return predictionTicks; }
    public static synchronized Mode getMode() { return mode; }
    public static synchronized TargetPriority getTargetPriority() { return targetPriority; }
    public static synchronized TargetArea getTargetArea() { return targetArea; }
    public static double getMaximumRange() { return 3.0D; }
    public static float getMaximumRotationStep() { return 1.5F; }

    public static synchronized void setCorrectionSpeed(int value) {
        correctionSpeed = clamp(value, 1, 30);
        save();
    }
    public static synchronized void setReactionDelayMs(int value) {
        reactionDelayMs = clamp(value, 0, 300);
        save();
    }
    public static synchronized void setMaximumAngleDegrees(int value) {
        maximumAngleDegrees = clamp(value, 1, 10);
        save();
    }
    public static synchronized void setHorizontalSpeed(int value) {
        horizontalSpeed = clamp(value, 1, 20);
        save();
    }
    public static synchronized void setVerticalSpeed(int value) {
        verticalSpeed = clamp(value, 1, 20);
        save();
    }
    public static synchronized void setPredictionTicks(float value) {
        predictionTicks = clamp(value, 0.0F, 10.0F);
        save();
    }
    public static synchronized void cycleMode() {
        Mode[] values = Mode.values();
        mode = values[(mode.ordinal() + 1) % values.length];
        save();
    }
    public static synchronized void cycleTargetPriority() {
        TargetPriority[] values = TargetPriority.values();
        targetPriority = values[(targetPriority.ordinal() + 1) % values.length];
        save();
    }
    public static synchronized void cycleTargetArea() {
        TargetArea[] values = TargetArea.values();
        targetArea = values[(targetArea.ordinal() + 1) % values.length];
        save();
    }
    public static synchronized void setTargetArea(TargetArea area) {
        if (area != null && targetArea != area) {
            targetArea = area;
            save();
        }
    }

    public static synchronized void reset() {
        correctionSpeed = DEFAULT_CORRECTION_SPEED;
        reactionDelayMs = DEFAULT_REACTION_DELAY_MS;
        maximumAngleDegrees = DEFAULT_MAX_ANGLE_DEGREES;
        horizontalSpeed = DEFAULT_HORIZONTAL_SPEED;
        verticalSpeed = DEFAULT_VERTICAL_SPEED;
        predictionTicks = DEFAULT_PREDICTION_TICKS;
        mode = Mode.SMOOTH;
        targetPriority = TargetPriority.CROSSHAIR;
        targetArea = TargetArea.FULL_BODY;
        save();
    }

    public static synchronized JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("mode", mode.name());
        json.addProperty("targetPriority", targetPriority.name());
        json.addProperty("correctionSpeed", correctionSpeed);
        json.addProperty("reactionDelayMs", reactionDelayMs);
        json.addProperty("maximumAngleDegrees", maximumAngleDegrees);
        json.addProperty("horizontalSpeed", horizontalSpeed);
        json.addProperty("verticalSpeed", verticalSpeed);
        json.addProperty("predictionTicks", predictionTicks);
        json.addProperty("targetArea", targetArea.name());
        return json;
    }

    public static synchronized void loadFromJson(JsonObject json) {
        if (json == null) return;
        correctionSpeed = readInt(json, "correctionSpeed", DEFAULT_CORRECTION_SPEED, 1, 30);
        reactionDelayMs = readInt(json, "reactionDelayMs", DEFAULT_REACTION_DELAY_MS, 0, 300);
        maximumAngleDegrees = readInt(json, "maximumAngleDegrees", DEFAULT_MAX_ANGLE_DEGREES, 1, 10);
        horizontalSpeed = readInt(json, "horizontalSpeed", DEFAULT_HORIZONTAL_SPEED, 1, 20);
        verticalSpeed = readInt(json, "verticalSpeed", DEFAULT_VERTICAL_SPEED, 1, 20);
        predictionTicks = readFloat(json, "predictionTicks", DEFAULT_PREDICTION_TICKS, 0.0F, 10.0F);
        mode = readEnum(json, "mode", Mode.class, Mode.SMOOTH);
        targetPriority = readEnum(json, "targetPriority", TargetPriority.class, TargetPriority.CROSSHAIR);
        targetArea = readEnum(json, "targetArea", TargetArea.class, TargetArea.FULL_BODY);
    }

    private static void save() { AlphaClientConfig.save(); }

    private static int readInt(JsonObject json, String key, int fallback, int min, int max) {
        try {
            if (json.has(key)) return clamp(json.get(key).getAsInt(), min, max);
        } catch (Exception ignored) { }
        return fallback;
    }
    private static float readFloat(JsonObject json, String key, float fallback, float min, float max) {
        try {
            if (json.has(key)) return clamp(json.get(key).getAsFloat(), min, max);
        } catch (Exception ignored) { }
        return fallback;
    }
    private static <T extends Enum<T>> T readEnum(JsonObject json, String key, Class<T> type, T fallback) {
        try {
            if (json.has(key)) return Enum.valueOf(type, json.get(key).getAsString());
        } catch (Exception ignored) { }
        return fallback;
    }
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
