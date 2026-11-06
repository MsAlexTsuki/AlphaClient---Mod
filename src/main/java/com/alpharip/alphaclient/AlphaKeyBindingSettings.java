package com.alpharip.alphaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Persistent storage for AlphaClient-owned keybindings. */
public final class AlphaKeyBindingSettings {
    private static final Properties VALUES = new Properties();
    private static boolean loaded;
    private static boolean loadFailed;
    private static boolean dirty;

    private AlphaKeyBindingSettings() { }

    public static synchronized void load() {
        if (loaded) return;
        FileInputStream input = null;
        try {
            File file = getFile();
            if (file.isFile()) {
                input = new FileInputStream(file);
                VALUES.load(input);
            }
        } catch (Exception error) {
            loadFailed = true;
            System.err.println("[AlphaClient] Could not load Alpha keybinds: " + error.getMessage());
        } finally {
            loaded = true;
            if (input != null) {
                try { input.close(); } catch (Exception ignored) { }
            }
        }
    }

    public static synchronized int getKeyCode(String id, int fallback) {
        load();
        String stored = VALUES.getProperty(id);
        if (stored == null) return fallback;
        try {
            int keyCode = Integer.parseInt(stored);
            // LWJGL 2 uses negative values for mouse buttons (-100 and below).
            return keyCode >= -100 && keyCode <= 65535 ? keyCode : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static synchronized void saveBindings(String[] ids, KeyBinding[] bindings) {
        load();
        if (loadFailed || ids == null || bindings == null || ids.length != bindings.length) return;

        boolean changed = false;
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == null || bindings[i] == null) continue;
            String keyCode = String.valueOf(bindings[i].getKeyCode());
            if (!keyCode.equals(VALUES.getProperty(ids[i]))) {
                VALUES.setProperty(ids[i], keyCode);
                changed = true;
            }
        }
        if (changed) dirty = true;
        if (!dirty) return;

        File destination = getFile();
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.err.println("[AlphaClient] Could not create Alpha keybind directory.");
            return;
        }

        File temporary = new File(parent, destination.getName() + ".tmp");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(temporary);
            VALUES.store(output, "AlphaClient keybinds");
            output.close();
            output = null;
            try {
                Files.move(temporary.toPath(), destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception atomicFailure) {
                Files.move(temporary.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not save Alpha keybinds: " + error.getMessage());
        } finally {
            if (output != null) {
                try { output.close(); } catch (Exception ignored) { }
            }
        }
    }

    private static File getFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File base = minecraft == null ? new File(".") : minecraft.mcDataDir;
        return new File(new File(new File(base, "config"), "alphaclient"), "keybinds.properties");
    }
}
