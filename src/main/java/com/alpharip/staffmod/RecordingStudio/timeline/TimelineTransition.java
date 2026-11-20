package com.alpharip.staffmod.RecordingStudio.timeline;

/** A user-selected transition between two specific adjacent video clips. */
public final class TimelineTransition {
    private final String fromClipId;
    private final String toClipId;
    private final TimelineTransitionType type;
    private final long durationMillis;

    public TimelineTransition(String fromClipId, String toClipId,
                              TimelineTransitionType type, long durationMillis) {
        if (blank(fromClipId) || blank(toClipId) || fromClipId.equals(toClipId)
                || type == null || durationMillis < 1L) {
            throw new IllegalArgumentException("Invalid timeline transition");
        }
        this.fromClipId = fromClipId;
        this.toClipId = toClipId;
        this.type = type;
        this.durationMillis = durationMillis;
    }

    public String getFromClipId() { return fromClipId; }
    public String getToClipId() { return toClipId; }
    public TimelineTransitionType getType() { return type; }
    public long getDurationMillis() { return durationMillis; }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
