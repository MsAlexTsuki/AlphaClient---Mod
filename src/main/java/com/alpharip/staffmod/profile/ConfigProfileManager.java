package com.alpharip.staffmod.profile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores named snapshots of general client HUD configuration.
 * Module enable states remain owned by ModuleProfileManager.
 */
public final class ConfigProfileManager {
    private static final String HUD_FILE_NAME = "alphastaffmod_hud.properties";

    private ConfigProfileManager() {
    }

    private static File getConfigDirectory() {
        return new File(System.getProperty("user.dir"), "config");
    }

    private static File getHudConfigFile() {
        return new File(getConfigDirectory(), HUD_FILE_NAME);
    }

    private static File getProfilesDirectory() {
        return new File(new File(getConfigDirectory(), "alphaclient"), "config-profiles");
    }

    private static boolean ensureProfilesDirectory() {
        File directory = getProfilesDirectory();
        return directory.isDirectory() || directory.mkdirs();
    }

    private static String sanitizeName(String name) {
        if (name == null) return "";
        String safe = name.trim().replaceAll("[^A-Za-z0-9 _-]", "");
        if (safe.length() > 32) safe = safe.substring(0, 32).trim();
        return safe;
    }

    private static File getProfileFile(String name) {
        String safe = sanitizeName(name);
        if (safe.isEmpty()) return null;
        return new File(getProfilesDirectory(), safe + ".properties");
    }

    public static synchronized List<String> getProfiles() {
        File directory = getProfilesDirectory();
        File[] files = directory.listFiles();
        if (files == null) return Collections.emptyList();

        List<String> names = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".properties")) {
                names.add(file.getName().substring(0, file.getName().length() - ".properties".length()));
            }
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    /** Saves the current HUD layout and visual settings under a user-provided name. */
    public static synchronized boolean saveCurrent(String name) {
        String safe = sanitizeName(name);
        if (safe.isEmpty() || !ensureProfilesDirectory()) return false;

        File current = getHudConfigFile();
        if (!current.isFile()) return false;

        File target = getProfileFile(safe);
        try {
            Files.copy(current.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("[AlphaClient] Could not save config profile: " + e.getMessage());
            return false;
        }
    }

    /** Applies a saved HUD layout and visual settings without touching module profiles. */
    public static synchronized boolean loadProfile(String name) {
        File source = getProfileFile(name);
        if (source == null || !source.isFile()) return false;

        File target = getHudConfigFile();
        File parent = target.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) return false;

        try {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("[AlphaClient] Could not load config profile: " + e.getMessage());
            return false;
        }
    }

    public static synchronized boolean deleteProfile(String name) {
        File file = getProfileFile(name);
        return file != null && file.isFile() && file.delete();
    }
}
