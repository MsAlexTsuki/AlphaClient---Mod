package com.alpharip.staffmod.combo;

/** Immutable local settings for the Combo Breaker Cue prototype. */
public final class ComboBreakerConfig {
    private final long displayDurationMillis;
    private final boolean missEnabled;
    private final boolean blockedEnabled;
    private final boolean opponentHitEnabled;

    public ComboBreakerConfig() {
        this(900L, true, true, true);
    }

    public ComboBreakerConfig(long displayDurationMillis,
                              boolean missEnabled,
                              boolean blockedEnabled,
                              boolean opponentHitEnabled) {
        if (displayDurationMillis <= 0L) {
            throw new IllegalArgumentException("displayDurationMillis must be positive");
        }
        this.displayDurationMillis = displayDurationMillis;
        this.missEnabled = missEnabled;
        this.blockedEnabled = blockedEnabled;
        this.opponentHitEnabled = opponentHitEnabled;
    }

    public long getDisplayDurationMillis() {
        return displayDurationMillis;
    }

    public boolean isReasonEnabled(ComboBreakerCue.Reason reason) {
        if (reason == null) {
            return false;
        }
        switch (reason) {
            case MISS:
                return missEnabled;
            case BLOCKED:
                return blockedEnabled;
            case OPPONENT_HIT:
                return opponentHitEnabled;
            default:
                return false;
        }
    }
}
