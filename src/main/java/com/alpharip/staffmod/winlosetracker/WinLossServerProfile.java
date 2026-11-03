package com.alpharip.staffmod.winlosetracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Groups multiple addresses that belong to the same logical server.
 * Overall statistics include every address; address statistics remain separate.
 */
public final class WinLossServerProfile {
    private final String id;
    private final String displayName;
    private final Set<String> addresses = new LinkedHashSet<String>();
    private final List<WinLossRecord> records = new ArrayList<WinLossRecord>();

    public WinLossServerProfile(String id, String displayName) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Server profile id cannot be empty.");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Server display name cannot be empty.");
        }
        this.id = id.trim();
        this.displayName = displayName.trim();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }

    public synchronized boolean addAddress(String address) {
        String normalized = normalizeAddress(address);
        if (normalized.isEmpty()) return false;
        return addresses.add(normalized);
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
            throw new IllegalArgumentException("Address is not registered to this server profile.");
        }
        records.add(new WinLossRecord(normalized, result, timestamp));
    }

    /** Restores a previously persisted record after validating its address. */
    public synchronized boolean restoreRecord(String address, WinLossResult result, long timestamp) {
        String normalized = normalizeAddress(address);
        if (normalized.isEmpty() || !addresses.contains(normalized) || result == null) return false;
        records.add(new WinLossRecord(normalized, result, timestamp));
        return true;
    }

    public synchronized List<WinLossRecord> getRecords() {
        return Collections.unmodifiableList(new ArrayList<WinLossRecord>(records));
    }

    public synchronized WinLossStatistics getOverallStatistics() {
        return calculate(records);
    }

    public synchronized WinLossStatistics getAddressStatistics(String address) {
        String normalized = normalizeAddress(address);
        int wins = 0;
        int losses = 0;
        for (WinLossRecord record : records) {
            if (!record.getServerAddress().equals(normalized)) continue;
            if (record.getResult() == WinLossResult.WIN) wins++;
            else losses++;
        }
        return new WinLossStatistics(wins, losses);
    }

    public synchronized WinLossStatistics getStatisticsForAddresses(Collection<String> selectedAddresses) {
        Set<String> selected = new LinkedHashSet<String>();
        if (selectedAddresses != null) {
            for (String address : selectedAddresses) {
                String normalized = normalizeAddress(address);
                if (!normalized.isEmpty()) selected.add(normalized);
            }
        }
        int wins = 0;
        int losses = 0;
        for (WinLossRecord record : records) {
            if (!selected.contains(record.getServerAddress())) continue;
            if (record.getResult() == WinLossResult.WIN) wins++;
            else losses++;
        }
        return new WinLossStatistics(wins, losses);
    }

    private static WinLossStatistics calculate(List<WinLossRecord> source) {
        int wins = 0;
        int losses = 0;
        for (WinLossRecord record : source) {
            if (record.getResult() == WinLossResult.WIN) wins++;
            else losses++;
        }
        return new WinLossStatistics(wins, losses);
    }

    /**
     * Normalizes a hostname/address for matching. This does not resolve DNS,
     * discover aliases, or prove that two hostnames point to the same server.
     */
    public static String normalizeAddress(String address) {
        if (address == null) return "";
        String value = address.trim().toLowerCase(java.util.Locale.ROOT);
        while (value.endsWith(".")) value = value.substring(0, value.length() - 1);
        if (value.startsWith("[") && value.contains("]")) {
            int close = value.indexOf(']');
            value = value.substring(1, close);
        } else if (value.indexOf(':') >= 0 && value.indexOf(':') == value.lastIndexOf(':')) {
            int colon = value.lastIndexOf(':');
            String port = value.substring(colon + 1);
            if (port.matches("[0-9]+")) value = value.substring(0, colon);
        }
        return value;
    }
}
