package com.alpharip.staffmod.Palette;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Local storage for pinned palette actions and the twelve most recent actions. */
public final class PaletteStore {
    private static final PaletteStore INSTANCE = new PaletteStore();
    private static final int MAX_RECENT = 12;

    private final List<String> favorites = new ArrayList<String>();
    private final List<String> recent = new ArrayList<String>();
    private boolean loaded;
    private boolean loadFailed;

    private PaletteStore() {
    }

    public static PaletteStore getInstance() {
        return INSTANCE;
    }

    public synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        File file = getFile();
        if (!file.isFile()) {
            return;
        }

        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
            readIds(properties.getProperty("favorites", ""), favorites);
            readIds(properties.getProperty("recent", ""), recent);
        } catch (Exception exception) {
            loadFailed = true;
            favorites.clear();
            recent.clear();
            System.err.println("[AlphaClient] Palette history could not be loaded; the existing file will be left unchanged: "
                    + exception.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public synchronized boolean isWritable() {
        load();
        return !loadFailed;
    }

    public synchronized boolean isFavorite(String id) {
        load();
        return favorites.contains(id);
    }

    public synchronized List<String> getFavoriteIds() {
        load();
        return Collections.unmodifiableList(new ArrayList<String>(favorites));
    }

    public synchronized List<String> getRecentIds() {
        load();
        return Collections.unmodifiableList(new ArrayList<String>(recent));
    }

    public synchronized int recentIndex(String id) {
        load();
        return recent.indexOf(id);
    }

    public synchronized int favoriteIndex(String id) {
        load();
        return favorites.indexOf(id);
    }

    public synchronized boolean toggleFavorite(String id) {
        load();
        if (loadFailed || id == null || id.trim().isEmpty()) {
            return false;
        }

        boolean added;
        if (favorites.contains(id)) {
            favorites.remove(id);
            added = false;
        } else {
            favorites.add(id);
            added = true;
        }

        if (!save()) {
            if (added) {
                favorites.remove(id);
            } else if (!favorites.contains(id)) {
                favorites.add(id);
            }
            return false;
        }
        return added;
    }

    public synchronized void recordUsed(String id) {
        load();
        if (loadFailed || id == null || id.trim().isEmpty()) {
            return;
        }

        recent.remove(id);
        recent.add(0, id);
        while (recent.size() > MAX_RECENT) {
            recent.remove(recent.size() - 1);
        }
        save();
    }

    private File getFile() {
        Minecraft minecraft = Minecraft.getMinecraft();
        File gameDirectory = minecraft == null ? new File(".") : minecraft.mcDataDir;
        File directory = new File(gameDirectory, "config/alphaclient");
        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory()) {
            return new File(directory, "command-palette.properties");
        }
        return new File(directory, "command-palette.properties");
    }

    private static void readIds(String value, List<String> target) {
        target.clear();
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        String[] values = value.split(",");
        for (String item : values) {
            String id = item.trim();
            if (!id.isEmpty() && !target.contains(id)) {
                target.add(id);
            }
        }
    }

    private static String writeIds(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append(',');
            }
            result.append(value);
        }
        return result.toString();
    }

    private boolean save() {
        if (loadFailed) {
            return false;
        }

        File file = getFile();
        File temporary = new File(file.getParentFile(), file.getName() + ".tmp");
        Properties properties = new Properties();
        properties.setProperty("favorites", writeIds(favorites));
        properties.setProperty("recent", writeIds(recent));

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(temporary);
            properties.store(output, "AlphaClient Command Palette");
            output.close();
            output = null;

            try {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception exception) {
            System.err.println("[AlphaClient] Could not save palette history: " + exception.getMessage());
            return false;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {
                }
            }
            if (temporary.exists()) {
                temporary.delete();
            }
        }
    }
}
