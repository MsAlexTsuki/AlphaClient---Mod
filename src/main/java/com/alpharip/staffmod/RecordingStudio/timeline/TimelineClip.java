package com.alpharip.staffmod.RecordingStudio.timeline;

/** Reusable media clip; timeline positions and durations are milliseconds. */
public final class TimelineClip {
    private final String id, source;
    private long start, duration;
    private double speed = 1.0D;
    private TimelineVideoEffect effect = TimelineVideoEffect.NONE;
    private long fadeInMillis;
    private long fadeOutMillis;

    public TimelineClip(String id, String source, long start, long duration) {
        if (blank(id) || blank(source) || start < 0 || duration < 0) {
            throw new IllegalArgumentException("clip");
        }
        this.id = id.trim();
        this.source = source.trim();
        this.start = start;
        this.duration = duration;
    }

    public String getId() { return id; }
    public String getSource() { return source; }
    public synchronized long getStart() { return start; }
    public synchronized long getDuration() { return duration; }
    public synchronized long getEnd() {
        try { return Math.addExact(start, duration); }
        catch (ArithmeticException overflow) { return Long.MAX_VALUE; }
    }

    public synchronized void setStart(long value) {
        if (value < 0) throw new IllegalArgumentException("Clip start must be non-negative");
        start = value;
    }

    public synchronized void setDuration(long value) {
        if (value <= 0) throw new IllegalArgumentException("Clip duration must be positive");
        duration = value;
        fadeInMillis = Math.min(fadeInMillis, duration);
        fadeOutMillis = Math.min(fadeOutMillis, Math.max(0L, duration - fadeInMillis));
    }

    public synchronized double getSpeed() { return speed; }
    public synchronized void setSpeed(double value) {
        if (value <= 0 || value > 100 || Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Invalid clip speed");
        }
        speed = value;
    }

    public synchronized TimelineVideoEffect getEffect() { return effect; }
    public synchronized void setEffect(TimelineVideoEffect value) {
        if (value == null) throw new IllegalArgumentException("effect");
        effect = value;
    }

    public synchronized long getFadeInMillis() { return fadeInMillis; }
    public synchronized void setFadeInMillis(long value) {
        if (value < 0 || value > duration - fadeOutMillis) {
            throw new IllegalArgumentException("Fade in and fade out must fit inside the clip duration");
        }
        fadeInMillis = value;
    }

    public synchronized long getFadeOutMillis() { return fadeOutMillis; }
    public synchronized void setFadeOutMillis(long value) {
        if (value < 0 || value > duration - fadeInMillis) {
            throw new IllegalArgumentException("Fade in and fade out must fit inside the clip duration");
        }
        fadeOutMillis = value;
    }

    private static boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
