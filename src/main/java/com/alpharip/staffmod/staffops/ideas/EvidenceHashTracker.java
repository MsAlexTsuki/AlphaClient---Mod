package com.alpharip.staffmod.staffops.ideas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes a SHA-256 digest for bytes explicitly supplied by the caller.
 * This does not prove where evidence came from or establish chain of custody.
 */
public final class EvidenceHashTracker {
    public String sha256(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                result.append(String.format("%02x", value & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}