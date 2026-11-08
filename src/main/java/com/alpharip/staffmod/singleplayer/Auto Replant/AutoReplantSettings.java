package com.alpharip.staffmod.singleplayer.autoreplant;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Stores Auto Replant settings separately for each singleplayer world. */
public final class AutoReplantSettings {
    private static boolean enabled;
    private static String loadedWorld;
    private static File activeSettingsFile;
    private static boolean loaded;
    private static boolean loadFailed;

    private AutoReplantSettings() { }

    public static synchronized boolean isEnabled() {
        return enabled;
    }

    public static synchronized void setEnabled(boolean value) {
        if (!loaded || loadFailed || activeSettingsFile == null) return;
        enabled = value;
        save();
    }

    /** Called from the client tick so server-side block events never need to access Minecraft.getMinecraft(). */
    public static synchronized void loadForCurrentWorld(Minecraft mc) {
        if (!AutoReplantAccess.isAvailable(mc)) {
            loaded = false;
            loadedWorld = null;
            activeSettingsFile = null;
            enabled = false;
            loadFailed = false;
            return;
        }

        String worldName = mc.getIntegratedServer().getFolderName();
        if (worldName == null || worldName.trim().isEmpty()) return;
        if (loaded && worldName.equals(loadedWorld)) return;

        loadedWorld = worldName;
        activeSettingsFile = settingsFile(mc, worldName);
        loaded = true;
        loadFailed = false;
        enabled = false;

        if (activeSettingsFile == null || !activeSettingsFile.isFile()) return;

        Properties values = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(activeSettingsFile);
            values.load(input);
            enabled = Boolean.parseBoolean(values.getProperty("enabled", "false"));
        } catch (Exception error) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load Auto Replant settings; protecting the existing file: "
                    + error.getMessage());
        } finally {
            if (input != null) try { input.close(); } catch (IOException ignored) { }
        }
    }

    private static void save() {
        if (!loaded || loadFailed || activeSettingsFile == null) return;
        File parent = activeSettingsFile.getParentFile();
        if (parent == null && !activeSettingsFile.getParentFile().mkdirs()) return;
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) return;

        File temporary = new File(parent, activeSettingsFile.getName() + ".tmp");
        Properties values = new Properties();
        values.setProperty("enabled", String.valueOf(enabled));
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(temporary);
            values.store(output, "AlphaClient Auto Replant settings");
            output.close();
            output = null;
            try {
                Files.move(temporary.toPath(), activeSettingsFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                Files.move(temporary.toPath(), activeSettingsFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not save Auto Replant settings: " + error.getMessage());
        } finally {
            if (output != null) try { output.close(); } catch (IOException ignored) { }
            if (temporary.exists()) temporary.delete();
        }
    }

    private static File settingsFile(Minecraft mc, String worldName) {
        try {
            File saves = new File(mc.mcDataDir, "saves").getCanonicalFile();
            File world = new File(saves, worldName).getCanonicalFile();
            String root = saves.getCanonicalPath() + File.separator;
            if (!world.getPath().startsWith(root)) return null;
            return new File(new File(world, "alphaclient"), "auto-replant.properties");
        } catch (IOException invalidPath) {
            return null;
        }
    }
}
