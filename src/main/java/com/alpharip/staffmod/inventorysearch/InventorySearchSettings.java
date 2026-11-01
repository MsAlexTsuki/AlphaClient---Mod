package com.alpharip.staffmod.inventorysearch;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Persists the local InventorySearch enabled setting. */
public final class InventorySearchSettings {
    private static boolean enabled;
    private static boolean loaded;
    private static final File FILE = new File(
            new File(Minecraft.getMinecraft().mcDataDir, "config/alphaclient"),
            "inventorysearch.properties"
    );

    private InventorySearchSettings() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void load() {
        if (loaded) {
            return;
        }

        loaded = true;
        if (!FILE.isFile()) {
            return;
        }

        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(FILE)) {
            properties.load(input);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", "false"));
        } catch (IOException ignored) {
            enabled = false;
        }
    }

    public static void setEnabled(boolean value) {
        load();
        enabled = value;

        File parent = FILE.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        try (FileOutputStream output = new FileOutputStream(FILE)) {
            properties.store(output, "AlphaClient InventorySearch");
        } catch (IOException ignored) {
            // Keep the in-memory setting if the local configuration cannot be written.
        }
    }
}
