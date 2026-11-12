package com.alpharip.staffmod.Viewer.PlayerDirectionViewer;

import java.util.Locale;

/** Converts Minecraft yaw/pitch values into compact human-readable directions. */
public final class PlayerDirectionFormatter {
    private static final String[] DIRECTIONS =
            {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    private PlayerDirectionFormatter() { }

    public static String compass(float yaw) {
        int index = Math.round(normalize(yaw) / 45.0F) & 7;
        return DIRECTIONS[index];
    }

    public static String angles(float yaw, float pitch) {
        return String.format(Locale.ROOT, "Yaw %.1f  Pitch %.1f",
                normalizeSigned(yaw), clampPitch(pitch));
    }

    public static float normalize(float yaw) {
        float result = yaw % 360.0F;
        return result < 0.0F ? result + 360.0F : result;
    }

    private static float normalizeSigned(float angle) {
        float result = angle % 360.0F;
        if (result >= 180.0F) result -= 360.0F;
        if (result < -180.0F) result += 360.0F;
        return result;
    }

    private static float clampPitch(float pitch) {
        return Math.max(-90.0F, Math.min(90.0F, pitch));
    }
}
