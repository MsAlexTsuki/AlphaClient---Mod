package com.alpharip.staffmod.singleplayer.treeexcavate;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Stores Tree Excavate settings inside the active singleplayer world's AlphaClient folder. */
public final class TreeExcavateSettings {
    private static final Properties VALUES = new Properties();
    private static String loadedWorld;
    private static boolean loaded;
    private static boolean loadFailed;

    private TreeExcavateSettings() { }

    public static synchronized boolean isEnabled() {
        ensureLoaded();
        return Boolean.parseBoolean(VALUES.getProperty("enabled", "false"));
    }

    public static synchronized void setEnabled(boolean enabled) {
        ensureLoaded();
        VALUES.setProperty("enabled", String.valueOf(enabled));
        save();
    }

    public static synchronized int getMaxLogs() {
        ensureLoaded();
        try {
            int value = Integer.parseInt(VALUES.getProperty("maxLogs", "64"));
            return Math.max(1, Math.min(128, value));
        } catch (NumberFormatException ignored) {
            return 64;
        }
    }

    public static synchronized void setMaxLogs(int maxLogs) {
        ensureLoaded();
        VALUES.setProperty("maxLogs", String.valueOf(Math.max(1, Math.min(128, maxLogs))));
        save();
    }

    private static void ensureLoaded() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!TreeExcavateAccess.isAvailable(mc)) return;

        String worldName = mc.getIntegratedServer().getFolderName();
        if (worldName == null || worldName.trim().isEmpty()) return;
        if (loaded && worldName.equals(loadedWorld)) return;

        loadedWorld = worldName;
        loaded = true;
        loadFailed = false;
        VALUES.clear();

        File file = settingsFile(mc, worldName);
        if (file == null || !file.isFile()) return;

        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            VALUES.load(input);
        } catch (Exception error) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load Tree Excavate settings; protecting the existing file: "
                    + error.getMessage());
        } finally {
            if (input != null) try { input.close(); } catch (IOException ignored) { }
        }
    }

    private static void save() {
        Minecraft mc = Minecraft.getMinecraft();
        if (loadFailed || !TreeExcavateAccess.isAvailable(mc) || loadedWorld == null) return;

        File destination = settingsFile(mc, loadedWorld);
        if (destination == null) return;
        File parent = destination.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs() && !parent.isDirectory())) return;

        File temporary = new File(parent, destination.getName() + ".tmp");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(temporary);
            VALUES.store(output, "AlphaClient Tree Excavate settings");
            output.close();
            output = null;
            try {
                java.nio.file.Files.move(temporary.toPath(), destination.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                java.nio.file.Files.move(temporary.toPath(), destination.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not save Tree Excavate settings: " + error.getMessage());
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
            return new File(new File(world, "alphaclient"), "tree-excavate.properties");
        } catch (IOException invalidPath) {
            return null;
        }
    }
}
