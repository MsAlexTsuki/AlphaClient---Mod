package com.alpharip.staffmod.staffops.ideas;

import java.time.Instant;

/**
 * Foundation for temporary user blacklists.
 * TODO(Supabase): persist entries and expiry timestamps.
 * Runtime enforcement, permissions, UI integration, and tests remain to be implemented.
 */
public class TemporaryBlacklistWithExpiry {
    private String playerId;
    private String reason;
    private String issuedBy;
    private Instant expiresAt;
    private boolean enabled = true;

    public TemporaryBlacklistWithExpiry(String playerId, String reason, String issuedBy, Instant expiresAt) {
        this.playerId = playerId;
        this.reason = reason;
        this.issuedBy = issuedBy;
        this.expiresAt = expiresAt;
    }

    public String getPlayerId() { return playerId; }
    public String getReason() { return reason; }
    public String getIssuedBy() { return issuedBy; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public boolean isExpired(Instant now) { return expiresAt != null && now != null && !now.isBefore(expiresAt); }
}