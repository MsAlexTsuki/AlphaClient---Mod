package com.alpharip.staffmod.Singleplayer.EasyCraft;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Local favorites collection; it never uses an account or remote service. */
public final class EasyCraftFavorites {
    private final Set<String> keys = new LinkedHashSet<String>();
    private final File file;

    public EasyCraftFavorites(File minecraftDirectory) {
        if (minecraftDirectory == null) throw new IllegalArgumentException("minecraftDirectory cannot be null");
        this.file = new File(new File(minecraftDirectory, "config/alphaclient"), "easycraft-favorites.properties");
        keys.addAll(EasyCraftFavoriteStorage.load(file));
    }

    public synchronized boolean contains(String key) {
        return key != null && keys.contains(key);
    }

    public synchronized boolean toggle(String key) {
        if (key == null || key.trim().isEmpty()) return false;
        boolean added;
        if (keys.contains(key)) {
            keys.remove(key);
            added = false;
        } else {
            keys.add(key);
            added = true;
        }
        EasyCraftFavoriteStorage.save(file, keys);
        return added;
    }

    public synchronized Set<String> snapshot() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(keys));
    }
}
