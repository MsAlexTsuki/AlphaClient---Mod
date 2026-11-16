package com.alpharip.staffmod.punishmentconflictdetector;

/** Immutable local representation of a punishment record. */
public final class PunishmentRecord {
    /** Use 0L for a permanent punishment. */
    public static final long PERMANENT = 0L;

    private final String recordId;
    private final String targetName;
    private final PunishmentType type;
    private final String reason;
    private final long issuedAt;
    private final long expiresAt;
    private final boolean revoked;

    public PunishmentRecord(String recordId, String targetName, PunishmentType type,
                            String reason, long issuedAt, long expiresAt, boolean revoked) {
        if (recordId == null || recordId.trim().isEmpty()) {
            throw new IllegalArgumentException("Record ID must not be empty.");
        }
        if (targetName == null || targetName.trim().isEmpty()) {
            throw new IllegalArgumentException("Target name must not be empty.");
        }
        if (type == null) throw new IllegalArgumentException("Punishment type must not be null.");
        if (issuedAt < 0L) throw new IllegalArgumentException("Issued time must not be negative.");
        if (expiresAt < 0L || (expiresAt != PERMANENT && expiresAt < issuedAt)) {
            throw new IllegalArgumentException("Expiry must be permanent (0) or at/after issue time.");
        }
        this.recordId = recordId.trim();
        this.targetName = targetName.trim();
        this.type = type;
        this.reason = reason == null ? "" : reason.trim();
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public String getRecordId() { return recordId; }
    public String getTargetName() { return targetName; }
    public PunishmentType getType() { return type; }
    public String getReason() { return reason; }
    public long getIssuedAt() { return issuedAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }

    public boolean isActiveAt(long timestamp) {
        return !revoked && issuedAt <= timestamp
                && (expiresAt == PERMANENT || timestamp < expiresAt);
    }

    String normalizedTarget() {
        return targetName.toLowerCase(java.util.Locale.ROOT);
    }
}
