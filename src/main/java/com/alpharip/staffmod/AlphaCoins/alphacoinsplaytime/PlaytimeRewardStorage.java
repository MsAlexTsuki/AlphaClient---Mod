package com.alpharip.staffmod.AlphaCoins.alphacoinsplaytime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/** Stores playtime reward progress locally; AlphaCoins balance storage remains owned by CORE. */
final class PlaytimeRewardStorage {
    private static final int VERSION = 1;

    Snapshot load(File file) {
        Snapshot snapshot = new Snapshot();
        if (file == null || !file.isFile()) return snapshot;

        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            if (parseInt(properties.getProperty("version"), 0) != VERSION) return snapshot;
            snapshot.totalPlaytimeMillis = Math.max(0L,
                    parseLong(properties.getProperty("totalPlaytimeMillis"), 0L));
            snapshot.dailyIntervalsAwarded = Math.max(0L,
                    parseLong(properties.getProperty("dailyIntervalsAwarded"), 0L));
            snapshot.tenDayIntervalsAwarded = Math.max(0L,
                    parseLong(properties.getProperty("tenDayIntervalsAwarded"), 0L));
            int count = clamp(parseInt(properties.getProperty("milestone.count"), 0), 0, 32);
            for (int i = 0; i < count; i++) {
                String id = properties.getProperty("milestone." + i);
                if (id != null && PlaytimeRewardDefinition.find(id) != null) {
                    snapshot.claimedMilestones.add(id);
                }
            }
        } catch (IOException ignored) {
            return new Snapshot();
        }
        return snapshot;
    }

    boolean save(File file, long totalPlaytimeMillis, long dailyIntervalsAwarded,
                 long tenDayIntervalsAwarded, Set<String> claimedMilestones) {
        if (file == null || totalPlaytimeMillis < 0L || dailyIntervalsAwarded < 0L
                || tenDayIntervalsAwarded < 0L || claimedMilestones == null) return false;

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) return false;

        Properties properties = new Properties();
        properties.setProperty("version", String.valueOf(VERSION));
        properties.setProperty("totalPlaytimeMillis", String.valueOf(totalPlaytimeMillis));
        properties.setProperty("dailyIntervalsAwarded", String.valueOf(dailyIntervalsAwarded));
        properties.setProperty("tenDayIntervalsAwarded", String.valueOf(tenDayIntervalsAwarded));
        properties.setProperty("milestone.count", String.valueOf(claimedMilestones.size()));
        int index = 0;
        for (String id : claimedMilestones) {
            properties.setProperty("milestone." + index++, id);
        }

        File temporary = new File(parent, file.getName() + ".tmp");
        try (FileOutputStream output = new FileOutputStream(temporary)) {
            properties.store(output, "AlphaCoins playtime reward progress");
        } catch (IOException error) {
            temporary.delete();
            return false;
        }

        try {
            Files.move(temporary.toPath(), file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (AtomicMoveNotSupportedException ignored) {
            try {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException error) {
                temporary.delete();
                return false;
            }
        } catch (IOException error) {
            temporary.delete();
            return false;
        }
    }

    private static int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (RuntimeException ignored) { return fallback; }
    }

    private static long parseLong(String value, long fallback) {
        try { return value == null ? fallback : Long.parseLong(value); }
        catch (RuntimeException ignored) { return fallback; }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static final class Snapshot {
        long totalPlaytimeMillis;
        long dailyIntervalsAwarded;
        long tenDayIntervalsAwarded;
        final Set<String> claimedMilestones = new LinkedHashSet<String>();
    }
}
