// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

/** Immutable local record for one result on one server address. */
public final class WinLossRecord {
    private final String serverAddress;
    private final WinLossResult result;
    private final long timestamp;

    public WinLossRecord(String serverAddress, WinLossResult result, long timestamp) {
        String normalizedAddress = WinLossServerProfile.normalizeAddress(serverAddress);
        if (normalizedAddress.isEmpty()) throw new IllegalArgumentException("Server address cannot be empty.");
        if (result == null) throw new IllegalArgumentException("Match result cannot be null.");
        this.serverAddress = normalizedAddress;
        this.result = result;
        this.timestamp = timestamp;
    }

    public String getServerAddress() { return serverAddress; }
    public WinLossResult getResult() { return result; }
    public long getTimestamp() { return timestamp; }
}
