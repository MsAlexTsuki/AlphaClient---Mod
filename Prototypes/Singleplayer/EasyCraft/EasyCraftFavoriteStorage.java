package com.alpharip.staffmod.Singleplayer.EasyCraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/** Reads and writes favorite recipe keys to a local properties file. */
final class EasyCraftFavoriteStorage {
    private EasyCraftFavoriteStorage() { }

    static Set<String> load(File file) {
        Set<String> result = new LinkedHashSet<String>();
        if (file == null || !file.isFile()) return result;
        Properties properties = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
            int count = Integer.parseInt(properties.getProperty("count", "0"));
            for (int i = 0; i < count; i++) {
                String key = properties.getProperty("favorite." + i);
                if (key != null && !key.trim().isEmpty()) result.add(key);
            }
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not load Easy Craft favorites: " + exception.getMessage());
        } finally {
            if (input != null) try { input.close(); } catch (Exception ignored) { }
        }
        return result;
    }

    static boolean save(File file, Set<String> favorites) {
        if (file == null || favorites == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;

        Properties properties = new Properties();
        int index = 0;
        for (String key : favorites) properties.setProperty("favorite." + index++, key);
        properties.setProperty("count", Integer.toString(index));

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            properties.store(output, "AlphaClient Easy Craft favorites");
            return true;
        } catch (Exception exception) {
            System.out.println("[AlphaClient] Could not save Easy Craft favorites: " + exception.getMessage());
            return false;
        } finally {
            if (output != null) try { output.close(); } catch (Exception ignored) { }
        }
    }
}
