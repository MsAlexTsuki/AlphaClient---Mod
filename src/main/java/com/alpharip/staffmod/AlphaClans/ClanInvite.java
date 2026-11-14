package com.alpharip.staffmod.AlphaClans;

import java.util.UUID;

public final class ClanInvite {
    private final UUID clanId;
    private final UUID inviterId;
    private final UUID targetId;
    private final long createdAt;
    private final long expiresAt;

    public ClanInvite(UUID clanId, UUID inviterId, UUID targetId, long createdAt, long expiresAt) {
        if (clanId == null || inviterId == null || targetId == null || inviterId.equals(targetId) || expiresAt <= createdAt) {
            throw new IllegalArgumentException("Invalid clan invite");
        }
        this.clanId = clanId;
        this.inviterId = inviterId;
        this.targetId = targetId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public UUID getClanId() { return clanId; }
    public UUID getInviterId() { return inviterId; }
    public UUID getTargetId() { return targetId; }
    public long getCreatedAt() { return createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public boolean isExpired(long now) { return now >= expiresAt; }
}
