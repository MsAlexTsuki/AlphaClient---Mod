package com.alpharip.staffmod.combo;

/**
 * Local prototype for notifying the player when their combat combo is interrupted.
 * Combat event detection and HUD rendering are not connected yet.
 */
public final class ComboBreakerCue {
    public enum Reason {
        MISS,
        BLOCKED,
        OPPONENT_HIT
    }

    public static final class Cue {
        public final Reason reason;
        public final long timestamp;
        public final long expiresAt;

        public Cue(Reason reason, long timestamp, long expiresAt) {
            if (reason == null) {
                throw new IllegalArgumentException("reason");
            }
            this.reason = reason;
            this.timestamp = timestamp;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired(long currentTimeMillis) {
            return currentTimeMillis >= expiresAt;
        }
    }

    private final ComboBreakerConfig config;
    private Cue activeCue;

    public ComboBreakerCue() {
        this(new ComboBreakerConfig());
    }

    public ComboBreakerCue(ComboBreakerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config");
        }
        this.config = config;
    }

    /** Creates and stores a cue for the supplied interruption reason. */
    public Cue trigger(Reason reason, long timestamp) {
        if (reason == null || !config.isReasonEnabled(reason)) {
            return null;
        }

        long expiresAt = timestamp + config.getDisplayDurationMillis();
        activeCue = new Cue(reason, timestamp, expiresAt);
        return activeCue;
    }

    /** Returns the active cue, clearing it when its display duration has elapsed. */
    public Cue getActiveCue(long currentTimeMillis) {
        if (activeCue != null && activeCue.isExpired(currentTimeMillis)) {
            activeCue = null;
        }
        return activeCue;
    }

    /** Retained as a lightweight cue factory for existing callers. */
    public Cue createCue(Reason reason, long timestamp) {
        if (reason == null) {
            return null;
        }
        return new Cue(reason, timestamp, timestamp + config.getDisplayDurationMillis());
    }

    public String getReasonLabel(Reason reason) {
        if (reason == Reason.MISS) {
            return "Missed";
        }
        if (reason == Reason.BLOCKED) {
            return "Blocked";
        }
        if (reason == Reason.OPPONENT_HIT) {
            return "Opponent hit you";
        }
        return "Unknown";
    }

    public void clear() {
        activeCue = null;
    }

    public ComboBreakerConfig getConfig() {
        return config;
    }
}
