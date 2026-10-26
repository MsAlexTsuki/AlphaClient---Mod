package com.alpharip.staffmod.staffops.ideas;

import java.time.Instant;

/**
 * Foundation for recording confirmed rank changes.
 * TODO(Supabase): persist confirmed change records and their audit trail.
 * Rank assignment integration, authorization checks, UI, and tests remain to be implemented.
 */
public class ConfirmedRankChanges {
    private String playerId;
    private String previousRank;
    private String confirmedRank;
    private String confirmedBy;
    private Instant confirmedAt;
    private boolean confirmed;

    public ConfirmedRankChanges(String playerId, String previousRank, String confirmedRank) {
        this.playerId = playerId;
        this.previousRank = previousRank;
        this.confirmedRank = confirmedRank;
    }

    public String getPlayerId() { return playerId; }
    public String getPreviousRank() { return previousRank; }
    public String getConfirmedRank() { return confirmedRank; }
    public String getConfirmedBy() { return confirmedBy; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public boolean isConfirmed() { return confirmed; }
    public void confirm(String staffId, Instant time) {
        if (staffId == null || staffId.trim().isEmpty() || time == null) return;
        confirmedBy = staffId;
        confirmedAt = time;
        confirmed = true;
    }
}