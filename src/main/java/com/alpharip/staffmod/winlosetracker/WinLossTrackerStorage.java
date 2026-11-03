package com.alpharip.staffmod.winlosetracker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
/** Persists server profiles, addresses, match records, and display preferences. */
public final class WinLossTrackerStorage {
    private WinLossTrackerStorage() { }
    public static void load(WinLossTracker tracker, File file) {
        if (tracker == null || file == null || !file.isFile()) return;
        Properties data = new Properties();
        try (FileInputStream input = new FileInputStream(file)) { data.load(input); }
        catch (IOException e) {
            System.err.println("[AlphaClient] Could not load Win/Loss Tracker data: " + e.getMessage());
            return;
        }
        WinLossTrackerSettings settings = tracker.getSettings();
        settings.setShowOverallStatistics(Boolean.parseBoolean(data.getProperty("settings.overall", "true")));
        settings.setShowCurrentAddressStatistics(Boolean.parseBoolean(data.getProperty("settings.current", "true")));
        settings.setShowIndividualAddressStatistics(Boolean.parseBoolean(data.getProperty("settings.individual", "false")));
        int count = readCount(data, "profiles.count");
        for (int i = 0; i < count; i++) {
            String prefix = "profile." + i + ".";
            String id = data.getProperty(prefix + "id", "").trim();
            String name = data.getProperty(prefix + "name", "").trim();
            if (id.isEmpty() || name.isEmpty()) continue;
            WinLossServerProfile profile;
            try { profile = new WinLossServerProfile(id, name); }
            catch (IllegalArgumentException ignored) { continue; }
            int addressCount = readCount(data, prefix + "addresses.count");
            for (int j = 0; j < addressCount; j++) profile.addAddress(data.getProperty(prefix + "address." + j, ""));
            if (!tracker.addServerProfile(profile)) continue;
            int recordCount = readCount(data, prefix + "records.count");
            for (int j = 0; j < recordCount; j++) {
                String rp = prefix + "record." + j + ".";
                String address = data.getProperty(rp + "address", "");
                try {
                    WinLossResult result = WinLossResult.valueOf(data.getProperty(rp + "result", ""));
                    if (profile.containsAddress(address)) profile.record(address, result, readLong(data, rp + "timestamp", 0L));
                } catch (IllegalArgumentException ignored) { }
            }
        }
    }
    public static void save(WinLossTracker tracker, File file) {
        if (tracker == null || file == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            System.err.println("[AlphaClient] Could not create Win/Loss Tracker data directory.");
            return;
        }
        Properties data = new Properties();
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
            Set<String> addresses = profile.getAddresses();
            data.setProperty(prefix + "addresses.count", Integer.toString(addresses.size()));
            int a = 0;
            for (String address : addresses) data.setProperty(prefix + "address." + a++, address);
            List<WinLossRecord> records = profile.getRecords();
            data.setProperty(prefix + "records.count", Integer.toString(records.size()));
            for (int j = 0; j < records.size(); j++) {
                WinLossRecord record = records.get(j);
                String rp = prefix + "record." + j + ".";
                data.setProperty(rp + "address", record.getServerAddress());
                data.setProperty(rp + "result", record.getResult().name());
                data.setProperty(rp + "timestamp", Long.toString(record.getTimestamp()));
            }
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            data.store(output, "AlphaClient Practice Win/Loss Tracker");
        } catch (IOException e) {
            System.err.println("[AlphaClient] Could not save Win/Loss Tracker data: " + e.getMessage());
        }
    }
    private static int readCount(Properties data, String key) {
        try { return Math.max(0, Math.min(100000, Integer.parseInt(data.getProperty(key, "0")))); }
        catch (NumberFormatException ignored) { return 0; }
    }
    private static long readLong(Properties data, String key, long fallback) {
        try { return Long.parseLong(data.getProperty(key, Long.toString(fallback))); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
