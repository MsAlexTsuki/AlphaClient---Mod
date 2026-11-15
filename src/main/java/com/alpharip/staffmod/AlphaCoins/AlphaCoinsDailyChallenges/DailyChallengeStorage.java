package com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Challenge-only local persistence. AlphaCoins wallet data is owned by the CORE package. */
final class DailyChallengeStorage {
    private static final int VERSION = 1;

    Snapshot load(File file) {
        Snapshot snapshot = new Snapshot();
        if (file == null || !file.isFile()) return snapshot;

        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            if (parseInt(properties.getProperty("version"), 0) != VERSION) return snapshot;

            snapshot.date = parseDate(properties.getProperty("date"));
            int challengeCount = clamp(parseInt(properties.getProperty("challenge.count"), 0), 0, 20);
            for (int i = 0; i < challengeCount; i++) {
                String prefix = "challenge." + i + ".";
                String id = properties.getProperty(prefix + "id");
                if (id != null && !id.trim().isEmpty()) {
                    snapshot.progress.put(id, new SavedProgress(
                            parseInt(properties.getProperty(prefix + "progress"), 0),
                            Boolean.parseBoolean(properties.getProperty(prefix + "claimed", "false"))));
                }
            }
        } catch (IOException ignored) {
            return new Snapshot();
        }
        return snapshot;
    }

    boolean save(File file, LocalDate date, List<DailyChallengeProgress> challenges) {
        if (file == null || date == null || challenges == null) return false;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) return false;

        Properties properties = new Properties();
        properties.setProperty("version", String.valueOf(VERSION));
        properties.setProperty("date", date.toString());
        properties.setProperty("challenge.count", String.valueOf(challenges.size()));
        for (int i = 0; i < challenges.size(); i++) {
            DailyChallengeProgress progress = challenges.get(i);
            String prefix = "challenge." + i + ".";
            properties.setProperty(prefix + "id", progress.getDefinition().getId());
            properties.setProperty(prefix + "progress", String.valueOf(progress.getProgress()));
            properties.setProperty(prefix + "claimed", String.valueOf(progress.isClaimed()));
        }

        File temporary = new File(parent, file.getName() + ".tmp");
        try (FileOutputStream output = new FileOutputStream(temporary)) {
            properties.store(output, "AlphaDailyChallenges local progress");
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

    private static LocalDate parseDate(String value) {
        try { return value == null ? null : LocalDate.parse(value); }
        catch (RuntimeException ignored) { return null; }
    }

    private static int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (RuntimeException ignored) { return fallback; }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static final class Snapshot {
        LocalDate date;
        final Map<String, SavedProgress> progress = new LinkedHashMap<String, SavedProgress>();
    }

    static final class SavedProgress {
        final int progress;
        final boolean claimed;

        SavedProgress(int progress, boolean claimed) {
            this.progress = progress;
            this.claimed = claimed;
        }
    }
}
