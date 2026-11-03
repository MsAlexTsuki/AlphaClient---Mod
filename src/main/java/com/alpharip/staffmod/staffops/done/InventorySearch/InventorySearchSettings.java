package com.alpharip.staffmod.InventorySearch;

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
    private static final File FILE = new File(new File(Minecraft.getMinecraft().mcDataDir, "config/alphaclient"), "inventorysearch.properties");
    private InventorySearchSettings() { }

    public static boolean isEnabled() { return enabled; }

    public static void load() {
        if (loaded) return;
        loaded = true;
        if (!FILE.isFile()) return;
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(FILE)) {
            properties.load(in);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", "false"));
        } catch (IOException ignored) {
            enabled = false;
        }
    }

    public static void setEnabled(boolean value) {
        load();
        enabled = value;
        File parent = FILE.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) return;
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        try (FileOutputStream out = new FileOutputStream(FILE)) {
            properties.store(out, "AlphaClient InventorySearch");
        } catch (IOException ignored) { }
    }
}