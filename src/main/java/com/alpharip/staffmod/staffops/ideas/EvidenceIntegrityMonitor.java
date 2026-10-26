package com.alpharip.staffmod.staffops.ideas;

/**
 * Compares two supplied digest strings as a basic local integrity check.
 * It cannot establish authenticity, provenance, or tamper-proof storage.
 */
public final class EvidenceIntegrityMonitor {
    public boolean matches(String expectedHash, String actualHash) {
        return expectedHash != null && actualHash != null
                && expectedHash.trim().equalsIgnoreCase(actualHash.trim());
    }

    public String describe(String expectedHash, String actualHash) {
        if (expectedHash == null || actualHash == null
                || expectedHash.trim().isEmpty() || actualHash.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return matches(expectedHash, actualHash) ? "MATCH" : "MISMATCH";
    }
}