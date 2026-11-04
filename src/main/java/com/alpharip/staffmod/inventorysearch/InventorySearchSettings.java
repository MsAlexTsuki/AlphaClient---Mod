package com.alpharip.staffmod.inventorysearch;

import net.minecraft.client.Minecraft;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Persists local InventorySearch settings. */
public final class InventorySearchSettings {
    public enum Mode {
        ALL("All"),
        INVENTORY("Inventory"),
        CHEST("Chest"),
        ENDER("Ender");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public Mode next() {
            Mode[] modes = values();
            return modes[(ordinal() + 1) % modes.length];
        }
    }

    private static boolean enabled;
    private static boolean loaded;
    private static Mode mode = Mode.ALL;
    private static final File FILE = new File(
            new File(Minecraft.getMinecraft().mcDataDir, "config/alphaclient"),
            "inventorysearch.properties"
    );

    private InventorySearchSettings() {
    }

    private static AlphaModuleFeature profileFeature() {
        return AlphaModuleRegistry.findFeature("inventory-search");
    }

    public static boolean isEnabled() {
        load();
        AlphaModuleFeature feature = profileFeature();
        return feature != null ? feature.isEnabled() : enabled;
    }

    public static Mode getMode() {
        load();
        return mode;
    }

    public static void setMode(Mode value) {
        load();
        if (value == null) {
            return;
        }
        mode = value;
        save();
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
            try {
                mode = Mode.valueOf(properties.getProperty("mode", "ALL").toUpperCase());
            } catch (IllegalArgumentException ignored) {
                mode = Mode.ALL;
            }
        } catch (IOException ignored) {
            enabled = false;
            mode = Mode.ALL;
        }
    }

    public static void setEnabled(boolean value) {
        load();
        AlphaModuleFeature feature = profileFeature();
        if (feature != null) feature.setEnabled(value);
        enabled = value;
        save();
    }

    private static void save() {
        File parent = FILE.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("mode", mode.name());
        try (FileOutputStream output = new FileOutputStream(FILE)) {
            properties.store(output, "AlphaClient InventorySearch");
        } catch (IOException ignored) {
            // Keep the in-memory settings if the local configuration cannot be written.
        }
    }
}
