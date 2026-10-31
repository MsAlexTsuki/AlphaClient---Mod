package com.alpharip.staffmod.staffops.ideas;
/** Combo-ending event model; combat event detection and visual flash rendering are pending. */
public final class ComboBreakerCue {
    public enum Reason { MISS, BLOCKED, OPPONENT_HIT }
    public static final class Cue {
        public final Reason reason;
        public final long timestamp;
        public Cue(Reason reason, long timestamp) {
            if (reason == null) throw new IllegalArgumentException("reason");
            this.reason = reason; this.timestamp = timestamp;
        }
    }
    public Cue createCue(Reason reason, long timestamp) {
        return reason == null ? null : new Cue(reason, timestamp);
    }
    public String getReasonLabel(Reason reason) {
        if (reason == Reason.MISS) return "Missed";
        if (reason == Reason.BLOCKED) return "Blocked";
        if (reason == Reason.OPPONENT_HIT) return "Opponent hit you";
        return "Unknown";
    }
}
