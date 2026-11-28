package com.alpharip.staffmod.animalbreedingcooldowntimer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/** Local properties persistence; this prototype does not use remote storage. */
public final class AnimalBreedingStorage {
    private final File file;

    public AnimalBreedingStorage(File configDirectory) {
        this.file = new File(configDirectory, "animalbreedingcooldowntimer.properties");
    }

    public void save(AnimalBreedingSettings settings,
                     List<AnimalBreedingEntry> entries) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()
                && !parent.isDirectory()) {
            throw new IOException("Could not create config directory");
        }
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(settings.isEnabled()));
        properties.setProperty("hudEnabled", Boolean.toString(settings.isHudEnabled()));
        properties.setProperty("intervalSeconds", Integer.toString(settings.getIntervalSeconds()));
        properties.setProperty("x", Integer.toString(settings.getX()));
        properties.setProperty("y", Integer.toString(settings.getY()));
        properties.setProperty("entryCount", Integer.toString(entries.size()));
        for (int i = 0; i < entries.size(); i++) {
            AnimalBreedingEntry entry = entries.get(i);
            String prefix = "entry." + i + ".";
            properties.setProperty(prefix + "id", entry.getAnimalId().toString());
            properties.setProperty(prefix + "name", encode(entry.getDisplayName()));
            properties.setProperty(prefix + "lastBred", Long.toString(entry.getLastBredAtMillis()));
            properties.setProperty(prefix + "cooldown", Integer.toString(entry.getCooldownSeconds()));
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "AlphaClient Animal Breeding Cooldown Timer");
        }
    }

    public void load(AnimalBreedingSettings settings,
                     AnimalBreedingTracker tracker) throws IOException {
        if (!file.isFile()) return;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        }
        settings.setEnabled(Boolean.parseBoolean(properties.getProperty("enabled", "true")));
        settings.setHudEnabled(Boolean.parseBoolean(properties.getProperty("hudEnabled", "true")));
        settings.setIntervalSeconds(parseInt(properties.getProperty("intervalSeconds"), 60));
        settings.setX(parseInt(properties.getProperty("x"), 8));
        settings.setY(parseInt(properties.getProperty("y"), 8));

        int count = Math.max(0, Math.min(256, parseInt(properties.getProperty("entryCount"), 0)));
        List<AnimalBreedingEntry> loaded = new ArrayList<AnimalBreedingEntry>();
        for (int i = 0; i < count; i++) {
            String prefix = "entry." + i + ".";
            try {
                UUID id = UUID.fromString(properties.getProperty(prefix + "id", ""));
                String name = decode(properties.getProperty(prefix + "name", ""));
                long lastBred = Long.parseLong(properties.getProperty(prefix + "lastBred", "0"));
                int cooldown = parseInt(properties.getProperty(prefix + "cooldown"), 60);
                loaded.add(new AnimalBreedingEntry(id, name, lastBred, cooldown));
            } catch (IllegalArgumentException ignored) {
                // Skip malformed records without discarding valid entries.
            }
        }
        tracker.replaceAll(loaded);
    }

    private static int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); }
        catch (RuntimeException ignored) { return fallback; }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        try {
            return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }
}
