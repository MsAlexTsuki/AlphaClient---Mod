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
    private String currentServerAddress = "";
    private WinLossTracker() { }
    public static WinLossTracker getInstance() { return INSTANCE; }
    public synchronized boolean addServerProfile(WinLossServerProfile profile) {
        if (profile == null || profiles.containsKey(profile.getId())) return false;
        for (String address : profile.getAddresses()) {
            if (findProfileByAddress(address) != null) return false;
        }
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
        String profileId = normalized;
        int suffix = 2;
        while (profiles.containsKey(profileId)) {
            profileId = normalized + "#" + suffix++;
        }
        WinLossServerProfile profile = new WinLossServerProfile(profileId, normalized);
        profile.addAddress(normalized);
        profiles.put(profileId, profile);
        return profile;
    }
    public synchronized List<WinLossServerProfile> getServerProfiles() {
        return Collections.unmodifiableList(new ArrayList<WinLossServerProfile>(profiles.values()));
    }
    public WinLossTrackerSettings getSettings() { return settings; }

    /** Updates the exact hostname currently connected in Minecraft. */
    public synchronized void setCurrentServerAddress(String address) {
        currentServerAddress = WinLossServerProfile.normalizeAddress(address);
        if (!currentServerAddress.isEmpty()) getOrCreateProfileForAddress(currentServerAddress);
    }

    public synchronized String getCurrentServerAddress() { return currentServerAddress; }

    public synchronized WinLossServerProfile getCurrentServerProfile() {
        return findProfileByAddress(currentServerAddress);
    }

    public synchronized WinLossStatistics getCurrentAddressStatistics() {
        WinLossServerProfile profile = getCurrentServerProfile();
        return profile == null ? new WinLossStatistics(0, 0) : profile.getAddressStatistics(currentServerAddress);
    }

    public synchronized WinLossStatistics getCurrentOverallStatistics() {
        WinLossServerProfile profile = getCurrentServerProfile();
        return profile == null ? new WinLossStatistics(0, 0) : profile.getOverallStatistics();
    }

    public synchronized boolean addAddressToProfile(String profileId, String address) {
        WinLossServerProfile profile = getServerProfile(profileId);
        String normalized = WinLossServerProfile.normalizeAddress(address);
        if (profile == null || normalized.isEmpty()) return false;
        WinLossServerProfile owner = findProfileByAddress(normalized);
        if (owner != null && owner != profile) return false;
        return profile.addAddress(normalized);
    }

    /** Records one already-confirmed Practice result for the connected address. */
    public synchronized boolean recordCurrentMatch(WinLossResult result, long timestamp) {
        if (currentServerAddress.isEmpty() || result == null) return false;
        return recordMatch(currentServerAddress, result, timestamp);
    }

    /** Restores a profile only when its ID and addresses do not conflict. */
    synchronized void restoreProfile(WinLossServerProfile profile) {
        if (profile == null || profiles.containsKey(profile.getId())) {
            throw new IllegalArgumentException("Duplicate or invalid stored server profile.");
        }
        for (String address : profile.getAddresses()) {
            if (findProfileByAddress(address) != null) {
                throw new IllegalArgumentException("Stored server address belongs to multiple profiles.");
            }
        }
        profiles.put(profile.getId(), profile);
    }

    synchronized void clearProfilesForRestore() { profiles.clear(); }

    synchronized void restoreCurrentServerAddress(String address) {
        currentServerAddress = WinLossServerProfile.normalizeAddress(address);
    }
    public synchronized boolean recordMatch(String address, WinLossResult result, long timestamp) {
        if (result == null) return false;
        WinLossServerProfile profile = getOrCreateProfileForAddress(address);
        if (profile == null) return false;
        profile.record(address, result, timestamp);
        return true;
    }
}
