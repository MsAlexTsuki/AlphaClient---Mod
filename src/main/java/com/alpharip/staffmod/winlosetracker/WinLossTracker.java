package com.alpharip.staffmod.winlosetracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/** Core registry for Practice Win/Loss server profiles. */
public final class WinLossTracker {
    private static final WinLossTracker INSTANCE = new WinLossTracker();
    private final Map<String, WinLossServerProfile> profiles = new LinkedHashMap<String, WinLossServerProfile>();
    private final WinLossTrackerSettings settings = new WinLossTrackerSettings();
    private WinLossTracker() { }
    public static WinLossTracker getInstance() { return INSTANCE; }
    public synchronized boolean addServerProfile(WinLossServerProfile profile) {
        if (profile == null || profiles.containsKey(profile.getId())) return false;
        profiles.put(profile.getId(), profile);
        return true;
    }
    public synchronized WinLossServerProfile getServerProfile(String id) { return id == null ? null : profiles.get(id); }
    public synchronized WinLossServerProfile findProfileByAddress(String address) {
        for (WinLossServerProfile profile : profiles.values()) if (profile.containsAddress(address)) return profile;
        return null;
    }
    /** Creates a basic profile automatically for an address not seen before. */
    public synchronized WinLossServerProfile getOrCreateProfileForAddress(String address) {
        String normalized = WinLossServerProfile.normalizeAddress(address);
        if (normalized.isEmpty()) return null;
        WinLossServerProfile existing = findProfileByAddress(normalized);
        if (existing != null) return existing;
        WinLossServerProfile profile = new WinLossServerProfile(normalized, normalized);
        profile.addAddress(normalized);
        profiles.put(normalized, profile);
        return profile;
    }
    public synchronized List<WinLossServerProfile> getServerProfiles() {
        return Collections.unmodifiableList(new ArrayList<WinLossServerProfile>(profiles.values()));
    }
    public WinLossTrackerSettings getSettings() { return settings; }
    public synchronized boolean recordMatch(String address, WinLossResult result, long timestamp) {
        if (result == null) return false;
        WinLossServerProfile profile = getOrCreateProfileForAddress(address);
        if (profile == null) return false;
        profile.record(address, result, timestamp);
        return true;
    }
}
