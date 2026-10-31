package com.alpharip.staffmod.optimization;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

import java.io.File;
import java.io.FileWriter;

/**
 * Applies AlphaClient's initial FPS-related defaults once.
 * Afterward, Minecraft's saved options remain under the player's control.
 */
public final class FpsOptimization {
    private static final String MARKER_NAME = "alphaclient-fps-defaults-applied";

    private FpsOptimization() {
    }

    /**
     * @return true when initialization is complete, or false when Minecraft's
     * settings are not ready yet and the caller should try again next tick.
     */
    public static boolean applyOnce() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null || mc.mcDataDir == null) {
            return false;
        }

        File marker = new File(new File(mc.mcDataDir, "config"), MARKER_NAME);
        if (marker.isFile()) {
            return true;
        }

        GameSettings settings = mc.gameSettings;
        settings.enableVsync = false;
        settings.limitFramerate = 260; // Minecraft 1.8.9 slider maximum represents Unlimited.
        settings.particleSetting = 2;
        settings.saveOptions();

        File parent = marker.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.out.println("[AlphaClient] Could not create FPS optimization config directory.");
            return true;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(marker);
            writer.write("AlphaClient FPS defaults applied. Minecraft options are now controlled by the player.");
            writer.flush();
        } catch (Exception e) {
            System.out.println("[AlphaClient] Could not save FPS optimization marker: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignored) {
                }
            }
        }

        return true;
    }
}
