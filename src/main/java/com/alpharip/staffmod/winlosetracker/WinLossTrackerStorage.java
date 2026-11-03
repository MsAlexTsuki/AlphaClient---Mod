package com.alpharip.staffmod.winlosetracker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Local persistence for server profiles, aliases, match records, and settings. */
public final class WinLossTrackerStorage {
    private static final String FORMAT_VERSION = "1";
    private WinLossTrackerStorage() { }

    public static void save(WinLossTracker tracker, File file) throws IOException {
        if (tracker == null || file == null) throw new IllegalArgumentException("Tracker and file are required.");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Could not create Win/Loss Tracker directory.");
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
            for (int a = 0; a < addresses.size(); a++) data.setProperty(prefix + "address." + a, addresses.get(a));

            List<WinLossRecord> records = profile.getRecords();
            data.setProperty(prefix + "records.count", Integer.toString(records.size()));
            for (int r = 0; r < records.size(); r++) {
                WinLossRecord record = records.get(r);
                String rp = prefix + "record." + r + ".";
                data.setProperty(rp + "address", record.getServerAddress());
                data.setProperty(rp + "result", record.getResult().name());
                data.setProperty(rp + "timestamp", Long.toString(record.getTimestamp()));
            }
        }

        File temporary = new File(file.getAbsolutePath() + ".tmp");
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(temporary))) {
            data.store(output, "AlphaClient Practice Win/Loss Tracker");
        }
        if (file.exists() && !file.delete()) {
            temporary.delete();
            throw new IOException("Could not replace existing Win/Loss Tracker data.");
        }
        if (!temporary.renameTo(file)) {
            temporary.delete();
            throw new IOException("Could not finalize Win/Loss Tracker data.");
        }
    }

    public static boolean load(File file, WinLossTracker tracker) throws IOException {
        if (tracker == null || file == null) throw new IllegalArgumentException("Tracker and file are required.");
        if (!file.isFile()) return false;

        Properties data = new Properties();
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
            data.load(input);
        }
        if (!FORMAT_VERSION.equals(data.getProperty("format.version"))) {
            throw new IOException("Unsupported Win/Loss Tracker storage format.");
        }

        int profileCount = readCount(data, "profiles.count");
        List<WinLossServerProfile> restored = new ArrayList<WinLossServerProfile>();
        for (int i = 0; i < profileCount; i++) {
            String prefix = "profile." + i + ".";
            String id = data.getProperty(prefix + "id");
            String name = data.getProperty(prefix + "name");
            if (id == null || name == null) throw new IOException("Invalid stored server profile.");
            WinLossServerProfile profile;
            try { profile = new WinLossServerProfile(id, name); }
            catch (IllegalArgumentException exception) { throw new IOException("Invalid stored server profile.", exception); }

            int addressCount = readCount(data, prefix + "addresses.count");
            for (int a = 0; a < addressCount; a++) {
                String address = data.getProperty(prefix + "address." + a);
                if (address == null || !profile.addAddress(address)) throw new IOException("Invalid stored server address.");
            }

            int recordCount = readCount(data, prefix + "records.count");
            for (int r = 0; r < recordCount; r++) {
                String rp = prefix + "record." + r + ".";
                try {
                    String address = data.getProperty(rp + "address");
                    WinLossResult result = WinLossResult.valueOf(data.getProperty(rp + "result"));
                    long timestamp = Long.parseLong(data.getProperty(rp + "timestamp"));
                    if (!profile.restoreRecord(address, result, timestamp)) throw new IOException("Invalid stored match record.");
                } catch (RuntimeException exception) {
                    throw new IOException("Invalid stored match record.", exception);
                }
            }
            restored.add(profile);
        }

        // Do not partially replace live data if the file is malformed.
        tracker.clearProfilesForRestore();
        for (WinLossServerProfile profile : restored) tracker.restoreProfile(profile);
        WinLossTrackerSettings settings = tracker.getSettings();
        settings.setShowOverallStatistics(Boolean.parseBoolean(data.getProperty("settings.overall", "true")));
        settings.setShowCurrentAddressStatistics(Boolean.parseBoolean(data.getProperty("settings.current", "true")));
        settings.setShowIndividualAddressStatistics(Boolean.parseBoolean(data.getProperty("settings.individual", "false")));
        tracker.restoreCurrentServerAddress(data.getProperty("current.address", ""));
        return true;
    }

    private static int readCount(Properties data, String key) throws IOException {
        try {
            int value = Integer.parseInt(data.getProperty(key, "0"));
            if (value < 0 || value > 1000000) throw new NumberFormatException("Out of range");
            return value;
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid stored count: " + key, exception);
        }
    }
}
