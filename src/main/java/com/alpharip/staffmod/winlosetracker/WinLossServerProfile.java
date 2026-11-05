// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Represents one logical Practice server.
 * Explicitly linked hostnames share overall statistics while retaining
 * independent per-address statistics.
 */
public final class WinLossServerProfile {
    private final String id;
    private final String displayName;
    private final Set<String> addresses = new LinkedHashSet<String>();
    private final List<WinLossRecord> records = new ArrayList<WinLossRecord>();

    public WinLossServerProfile(String id, String displayName) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Profile ID cannot be empty.");
        if (displayName == null || displayName.trim().isEmpty()) throw new IllegalArgumentException("Profile name cannot be empty.");
        this.id = id.trim();
        this.displayName = displayName.trim();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }

    public synchronized boolean addAddress(String address) {
        String normalized = normalizeAddress(address);
        return !normalized.isEmpty() && addresses.add(normalized);
    }

    public synchronized boolean containsAddress(String address) {
        String normalized = normalizeAddress(address);
        return !normalized.isEmpty() && addresses.contains(normalized);
    }

    public synchronized Set<String> getAddresses() {
        return Collections.unmodifiableSet(new LinkedHashSet<String>(addresses));
    }

    public synchronized void record(String address, WinLossResult result, long timestamp) {
        String normalized = normalizeAddress(address);
        if (normalized.isEmpty() || !addresses.contains(normalized)) {
            throw new IllegalArgumentException("Address is not part of this server profile.");
        }
        records.add(new WinLossRecord(normalized, result, timestamp));
    }

    synchronized boolean restoreRecord(String address, WinLossResult result, long timestamp) {
        String normalized = normalizeAddress(address);
        if (normalized.isEmpty() || !addresses.contains(normalized) || result == null) return false;
        records.add(new WinLossRecord(normalized, result, timestamp));
        return true;
    }

    public synchronized List<WinLossRecord> getRecords() {
        return Collections.unmodifiableList(new ArrayList<WinLossRecord>(records));
    }

    public synchronized WinLossStatistics getOverallStatistics() {
        return calculate(records, null);
    }

    public synchronized WinLossStatistics getAddressStatistics(String address) {
        return calculate(records, Collections.singleton(normalizeAddress(address)));
    }

    public synchronized WinLossStatistics getStatisticsForAddresses(Collection<String> selectedAddresses) {
        Set<String> selected = new LinkedHashSet<String>();
        if (selectedAddresses != null) {
            for (String address : selectedAddresses) {
                String normalized = normalizeAddress(address);
                if (!normalized.isEmpty()) selected.add(normalized);
            }
        }
        return calculate(records, selected);
    }

    private static WinLossStatistics calculate(List<WinLossRecord> source, Set<String> selectedAddresses) {
        int wins = 0;
        int losses = 0;
        for (WinLossRecord record : source) {
            if (selectedAddresses != null && !selectedAddresses.contains(record.getServerAddress())) continue;
            if (record.getResult() == WinLossResult.WIN) wins++;
            else losses++;
        }
        return new WinLossStatistics(wins, losses);
    }

    /**
     * Normalizes hostnames and host:port values for exact matching.
     * This does not resolve DNS or infer that different hostnames are aliases.
     */
    public static String normalizeAddress(String address) {
        if (address == null) return "";
        String normalized = address.trim().toLowerCase(Locale.ROOT);
        while (normalized.endsWith(".")) normalized = normalized.substring(0, normalized.length() - 1);

        if (normalized.startsWith("[") && normalized.indexOf(']') > 0) {
            normalized = normalized.substring(1, normalized.indexOf(']'));
        } else {
            int firstColon = normalized.indexOf(':');
            int lastColon = normalized.lastIndexOf(':');
            if (firstColon >= 0 && firstColon == lastColon) {
                String possiblePort = normalized.substring(lastColon + 1);
                if (possiblePort.matches("[0-9]+")) normalized = normalized.substring(0, lastColon);
            }
        }
        return normalized;
    }
}
