// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Thread-safe registry and recording service for Practice match results. */
public final class WinLossTracker {
    private static final WinLossTracker INSTANCE = new WinLossTracker();

    private final Map<String, WinLossServerProfile> profiles =
            new LinkedHashMap<String, WinLossServerProfile>();
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

    public synchronized WinLossServerProfile getServerProfile(String id) {
        return id == null ? null : profiles.get(id);
    }

    public synchronized WinLossServerProfile findProfileByAddress(String address) {
        for (WinLossServerProfile profile : profiles.values()) {
            if (profile.containsAddress(address)) return profile;
        }
        return null;
    }

    /** Creates a separate profile for an address until an alias is linked explicitly. */
    public synchronized WinLossServerProfile getOrCreateProfileForAddress(String address) {
        String normalized = WinLossServerProfile.normalizeAddress(address);
        if (normalized.isEmpty()) return null;

        WinLossServerProfile existing = findProfileByAddress(normalized);
        if (existing != null) return existing;

        String profileId = normalized;
        int suffix = 2;
        while (profiles.containsKey(profileId)) profileId = normalized + "#" + suffix++;

        WinLossServerProfile profile = new WinLossServerProfile(profileId, normalized);
        profile.addAddress(normalized);
        profiles.put(profileId, profile);
        return profile;
    }

    public synchronized List<WinLossServerProfile> getServerProfiles() {
        return Collections.unmodifiableList(new ArrayList<WinLossServerProfile>(profiles.values()));
    }

    public WinLossTrackerSettings getSettings() { return settings; }

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
        return profile == null ? new WinLossStatistics(0, 0)
                : profile.getAddressStatistics(currentServerAddress);
    }

    public synchronized WinLossStatistics getCurrentOverallStatistics() {
        WinLossServerProfile profile = getCurrentServerProfile();
        return profile == null ? new WinLossStatistics(0, 0) : profile.getOverallStatistics();
    }

    public synchronized boolean addAddressToProfile(String profileId, String address) {
        WinLossServerProfile profile = getServerProfile(profileId);
        String normalized = WinLossServerProfile.normalizeAddress(address);
        if (profile == null || normalized.isEmpty()) return false;

        WinLossServerProfile existingOwner = findProfileByAddress(normalized);
        if (existingOwner != null && existingOwner != profile) return false;
        return profile.addAddress(normalized);
    }

    public synchronized boolean recordCurrentMatch(WinLossResult result, long timestamp) {
        if (currentServerAddress.isEmpty() || result == null) return false;
        return recordMatch(currentServerAddress, result, timestamp);
    }

    public synchronized boolean recordMatch(String address, WinLossResult result, long timestamp) {
        if (result == null) return false;
        String normalized = WinLossServerProfile.normalizeAddress(address);
        WinLossServerProfile profile = getOrCreateProfileForAddress(normalized);
        if (profile == null) return false;
        profile.record(normalized, result, timestamp);
        return true;
    }

    synchronized void clearProfilesForRestore() { profiles.clear(); }

    synchronized void restoreProfile(WinLossServerProfile profile) {
        if (profile == null || profiles.containsKey(profile.getId())) {
            throw new IllegalArgumentException("Invalid or duplicate stored profile.");
        }
        for (String address : profile.getAddresses()) {
            if (findProfileByAddress(address) != null) {
                throw new IllegalArgumentException("Stored address is assigned to multiple profiles.");
            }
        }
        profiles.put(profile.getId(), profile);
    }

    synchronized void restoreCurrentServerAddress(String address) {
        currentServerAddress = WinLossServerProfile.normalizeAddress(address);
    }
}
