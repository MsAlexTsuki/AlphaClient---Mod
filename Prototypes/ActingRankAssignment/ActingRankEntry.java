package com.alpharip.staffmod.actingrankassignment;

import java.util.UUID;

/** Immutable local record describing a temporary acting-rank assignment. */
public final class ActingRankEntry {
    private final String id;
    private final String staffName;
    private final String currentRank;
    private final String actingRank;
    private final String assignedBy;
    private final String reason;
    private final long assignedAtMillis;
    private final long expiresAtMillis;
    private final ActingRankStatus status;
    private final String revokedBy;
    private final long revokedAtMillis;

    public ActingRankEntry(String staffName, String currentRank, String actingRank,
            String assignedBy, String reason, long assignedAtMillis, long expiresAtMillis) {
        this(UUID.randomUUID().toString(), staffName, currentRank, actingRank, assignedBy,
                reason, assignedAtMillis, expiresAtMillis, ActingRankStatus.ACTIVE, "", 0L);
    }

    private ActingRankEntry(String id, String staffName, String currentRank, String actingRank,
            String assignedBy, String reason, long assignedAtMillis, long expiresAtMillis,
            ActingRankStatus status, String revokedBy, long revokedAtMillis) {
        this.id = id;
        this.staffName = staffName;
        this.currentRank = currentRank;
        this.actingRank = actingRank;
        this.assignedBy = assignedBy;
        this.reason = reason;
        this.assignedAtMillis = assignedAtMillis;
        this.expiresAtMillis = expiresAtMillis;
        this.status = status;
        this.revokedBy = revokedBy;
        this.revokedAtMillis = revokedAtMillis;
    }

    public String getId() { return id; }
    public String getStaffName() { return staffName; }
    public String getCurrentRank() { return currentRank; }
    public String getActingRank() { return actingRank; }
    public String getAssignedBy() { return assignedBy; }
    public String getReason() { return reason; }
    public long getAssignedAtMillis() { return assignedAtMillis; }
    public long getExpiresAtMillis() { return expiresAtMillis; }
    public ActingRankStatus getStatus() { return status; }
    public String getRevokedBy() { return revokedBy; }
    public long getRevokedAtMillis() { return revokedAtMillis; }

    public ActingRankStatus getEffectiveStatus(long nowMillis) {
        if (status == ActingRankStatus.ACTIVE && expiresAtMillis <= nowMillis) {
            return ActingRankStatus.EXPIRED;
        }
        return status;
    }

    public boolean isActiveAt(long nowMillis) {
        return getEffectiveStatus(nowMillis) == ActingRankStatus.ACTIVE;
    }

    public ActingRankEntry revoke(String actor, long nowMillis) {
        if (status != ActingRankStatus.ACTIVE || expiresAtMillis <= nowMillis) return this;
        return new ActingRankEntry(id, staffName, currentRank, actingRank, assignedBy,
                reason, assignedAtMillis, expiresAtMillis, ActingRankStatus.REVOKED,
                actor, nowMillis);
    }

    public ActingRankEntry markExpired() {
        if (status != ActingRankStatus.ACTIVE) return this;
        return new ActingRankEntry(id, staffName, currentRank, actingRank, assignedBy,
                reason, assignedAtMillis, expiresAtMillis, ActingRankStatus.EXPIRED,
                revokedBy, revokedAtMillis);
    }
}
