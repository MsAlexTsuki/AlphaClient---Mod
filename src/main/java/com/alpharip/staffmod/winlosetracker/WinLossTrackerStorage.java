// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Validated local persistence for profiles, aliases, results, and display settings. */
public final class WinLossTrackerStorage {
    private static final String FORMAT_VERSION = "1";

    private WinLossTrackerStorage() { }

    public static void save(WinLossTracker tracker, File file) throws IOException {
        if (tracker == null || file == null) {
            throw new IllegalArgumentException("Tracker and file are required.");
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Could not create the Win/Loss Tracker config directory.");
        }

        Properties data = new Properties();
        data.setProperty("format.version", FORMAT_VERSION);
        data.setProperty("current.address", tracker.getCurrentServerAddress());

        WinLossTrackerSettings settings = tracker.getSettings();
        data.setProperty("settings.overall", Boolean.toString(settings.isShowOverallStatistics()));
        data.setProperty("settings.current", Boolean.toString(settings.isShowCurrentAddressStatistics()));
        data.setProperty("settings.individual", Boolean.toString(settings.isShowIndividualAddressStatistics()));

        List<WinLossServerProfile> profiles = tracker.getServerProfiles();
        data.setProperty("profiles.count", Integer.toString(profiles.size()));
        for (int i = 0; i < profiles.size(); i++) {
            WinLossServerProfile profile = profiles.get(i);
            String prefix = "profile." + i + ".";
            data.setProperty(prefix + "id", profile.getId());
            data.setProperty(prefix + "name", profile.getDisplayName());

            List<String> addresses = new ArrayList<String>(profile.getAddresses());
            data.setProperty(prefix + "addresses.count", Integer.toString(addresses.size()));
            for (int a = 0; a < addresses.size(); a++) {
                data.setProperty(prefix + "address." + a, addresses.get(a));
            }

            List<WinLossRecord> records = profile.getRecords();
            data.setProperty(prefix + "records.count", Integer.toString(records.size()));
            for (int r = 0; r < records.size(); r++) {
                WinLossRecord record = records.get(r);
                String recordPrefix = prefix + "record." + r + ".";
                data.setProperty(recordPrefix + "address", record.getServerAddress());
                data.setProperty(recordPrefix + "result", record.getResult().name());
                data.setProperty(recordPrefix + "timestamp", Long.toString(record.getTimestamp()));
            }
        }

        File temporary = new File(file.getAbsolutePath() + ".tmp");
        try {
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(temporary));
            try {
                data.store(output, "AlphaClient Win/Loss Tracker");
            } finally {
                output.close();
            }

            try {
                Files.move(temporary.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                try {
                    Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException replacementFailure) {
                    replacementFailure.addSuppressed(atomicMoveFailure);
                    throw replacementFailure;
                }
            }
        } finally {
            if (temporary.exists() && !temporary.delete()) {
                System.err.println("[AlphaClient] Could not remove temporary Win/Loss Tracker file.");
            }
        }
    }

    /**
     * Loads and validates the complete file before replacing live tracker data.
     * Returns false when no saved file exists.
     */
    public static boolean load(File file, WinLossTracker tracker) throws IOException {
        if (tracker == null || file == null) {
            throw new IllegalArgumentException("Tracker and file are required.");
        }
        if (!file.isFile()) return false;

        Properties data = new Properties();
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
        try {
            data.load(input);
        } finally {
            input.close();
        }

        if (!FORMAT_VERSION.equals(data.getProperty("format.version"))) {
            throw new IOException("Unsupported Win/Loss Tracker storage format.");
        }

        int profileCount = readCount(data, "profiles.count");
        List<WinLossServerProfile> restoredProfiles = new ArrayList<WinLossServerProfile>();
        Set<String> profileIds = new HashSet<String>();
        Set<String> registeredAddresses = new HashSet<String>();

        for (int i = 0; i < profileCount; i++) {
            String prefix = "profile." + i + ".";
            String id = data.getProperty(prefix + "id");
            String name = data.getProperty(prefix + "name");
            if (id == null || name == null) throw new IOException("Invalid stored server profile.");

            WinLossServerProfile profile;
            try {
                profile = new WinLossServerProfile(id, name);
            } catch (IllegalArgumentException invalidProfile) {
                throw new IOException("Invalid stored server profile.", invalidProfile);
            }
            if (!profileIds.add(profile.getId())) throw new IOException("Duplicate stored profile ID.");

            int addressCount = readCount(data, prefix + "addresses.count");
            for (int a = 0; a < addressCount; a++) {
                String address = data.getProperty(prefix + "address." + a);
                String normalized = WinLossServerProfile.normalizeAddress(address);
                if (normalized.isEmpty() || !registeredAddresses.add(normalized)
                        || !profile.addAddress(normalized)) {
                    throw new IOException("Invalid or duplicate stored server address.");
                }
            }

            int recordCount = readCount(data, prefix + "records.count");
            for (int r = 0; r < recordCount; r++) {
                String recordPrefix = prefix + "record." + r + ".";
                try {
                    String address = data.getProperty(recordPrefix + "address");
                    String resultValue = data.getProperty(recordPrefix + "result");
                    String timestampValue = data.getProperty(recordPrefix + "timestamp");
                    if (resultValue == null || timestampValue == null) {
                        throw new IllegalArgumentException("Stored record is incomplete.");
                    }
                    WinLossResult result = WinLossResult.valueOf(resultValue);
                    long timestamp = Long.parseLong(timestampValue);
                    if (!profile.restoreRecord(address, result, timestamp)) {
                        throw new IOException("Invalid stored match record.");
                    }
                } catch (RuntimeException invalidRecord) {
                    throw new IOException("Invalid stored match record.", invalidRecord);
                }
            }
            restoredProfiles.add(profile);
        }

        boolean showOverall = readBoolean(data, "settings.overall", true);
        boolean showCurrent = readBoolean(data, "settings.current", true);
        boolean showIndividual = readBoolean(data, "settings.individual", false);
        String currentAddress = data.getProperty("current.address", "");

        // Apply only after every profile, address, record, and setting passed validation.
        tracker.clearProfilesForRestore();
        for (WinLossServerProfile profile : restoredProfiles) tracker.restoreProfile(profile);
        WinLossTrackerSettings settings = tracker.getSettings();
        settings.setShowOverallStatistics(showOverall);
        settings.setShowCurrentAddressStatistics(showCurrent);
        settings.setShowIndividualAddressStatistics(showIndividual);
        tracker.restoreCurrentServerAddress(currentAddress);
        return true;
    }

    private static boolean readBoolean(Properties data, String key, boolean defaultValue) throws IOException {
        String value = data.getProperty(key);
        if (value == null) return defaultValue;
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new IOException("Invalid stored setting: " + key);
    }

    private static int readCount(Properties data, String key) throws IOException {
        String value = data.getProperty(key);
        if (value == null) throw new IOException("Missing stored count: " + key);
        try {
            int count = Integer.parseInt(value);
            if (count < 0 || count > 1000000) throw new NumberFormatException("Out of range");
            return count;
        } catch (NumberFormatException invalidCount) {
            throw new IOException("Invalid stored count: " + key, invalidCount);
        }
    }
}
