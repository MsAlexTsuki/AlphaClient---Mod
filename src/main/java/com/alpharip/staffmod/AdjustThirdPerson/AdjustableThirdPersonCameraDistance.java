package com.alpharip.staffmod.AdjustThirdPerson;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Prototype settings for the independent rear and front third-person camera distances.
 * Runtime camera integration is intentionally not included in this prototype.
 */
public class AdjustableThirdPersonCameraDistance {
    public boolean enabled = true;
    public String style = "default";

    public static final float MIN_DISTANCE = 1.0F;
    public static final float MAX_DISTANCE = 10.0F;
    public static final float DEFAULT_DISTANCE = 5.0F;

    private static final Properties VALUES = new Properties();
    private static boolean loaded;

    private static float rearDistance = DEFAULT_DISTANCE;
    private static float frontDistance = DEFAULT_DISTANCE;

    private AdjustableThirdPersonCameraDistance() { }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;

        FileInputStream input = null;
        try {
            File file = settingsFile();
            if (file.isFile()) {
                input = new FileInputStream(file);
                VALUES.load(input);
                rearDistance = parseDistance(VALUES.getProperty("rearDistance"), DEFAULT_DISTANCE);
                frontDistance = parseDistance(VALUES.getProperty("frontDistance"), DEFAULT_DISTANCE);
            }
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not load third-person camera prototype settings: " + error.getMessage());
        } finally {
            if (input != null) {
                try { input.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static float parseDistance(String value, float fallback) {
        try {
            return normalize(Float.parseFloat(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static float normalize(float value) {
        float clamped = Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, value));
        return Math.round(clamped * 10.0F) / 10.0F;
    }

    public static synchronized float getRearDistance() {
        load();
        return rearDistance;
    }

    public static synchronized float getFrontDistance() {
        load();
        return frontDistance;
    }

    public static synchronized void setRearDistance(float value) {
        load();
        rearDistance = normalize(value);
    }

    public static synchronized void setFrontDistance(float value) {
        load();
        frontDistance = normalize(value);
    }

    /** Saves both slider values independently to the local AlphaClient config folder. */
    public static synchronized void save() {
        load();
        File destination = settingsFile();
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            System.err.println("[AlphaClient] Could not create third-person camera settings directory.");
            return;
        }

        VALUES.setProperty("rearDistance", String.valueOf(rearDistance));
        VALUES.setProperty("frontDistance", String.valueOf(frontDistance));

        File temporary = new File(parent, destination.getName() + ".tmp");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(temporary);
            VALUES.store(output, "AlphaClient third-person camera prototype");
            output.close();
            output = null;

            try {
                java.nio.file.Files.move(temporary.toPath(), destination.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                java.nio.file.Files.move(temporary.toPath(), destination.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not save third-person camera prototype settings: " + error.getMessage());
        } finally {
            if (output != null) {
                try { output.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File settingsFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File gameDirectory = minecraft == null ? new File(".") : minecraft.mcDataDir;
        return new File(new File(gameDirectory, "config/alphaclient"), "third-person-camera.properties");
    }
}
