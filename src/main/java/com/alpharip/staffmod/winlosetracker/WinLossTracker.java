package com.alpharip.staffmod.winlosetracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core server-profile registry for Win/Loss Tracker.
 *
 * This is a data foundation only: Minecraft connection events, match-result
 * detection, GUI/HUD rendering, and disk persistence are not connected yet.
 */
public final class WinLossTracker {
    private final Map<String, WinLossServerProfile> profiles =
            new LinkedHashMap<String, WinLossServerProfile>();
    private final WinLossTrackerSettings settings = new WinLossTrackerSettings();

    public synchronized boolean addServerProfile(WinLossServerProfile profile) {
        if (profile == null || profiles.containsKey(profile.getId())) return false;
        profiles.put(profile.getId(), profile);
        return true;
    }

    public synchronized WinLossServerProfile getServerProfile(String profileId) {
        return profileId == null ? null : profiles.get(profileId);
    }

    public synchronized WinLossServerProfile findProfileByAddress(String address) {
        for (WinLossServerProfile profile : profiles.values()) {
            if (profile.containsAddress(address)) return profile;
        }
        return null;
    }

    public synchronized List<WinLossServerProfile> getServerProfiles() {
        return Collections.unmodifiableList(
                new ArrayList<WinLossServerProfile>(profiles.values()));
    }

    public WinLossTrackerSettings getSettings() {
        return settings;
    }

    /**
     * Records a match only when the address has explicitly been assigned
     * to a server profile. Returns false for unknown addresses.
     */
    public synchronized boolean recordMatch(String address, WinLossResult result, long timestamp) {
        WinLossServerProfile profile = findProfileByAddress(address);
        if (profile == null || result == null) return false;
        profile.record(address, result, timestamp);
        return true;
    }
}
