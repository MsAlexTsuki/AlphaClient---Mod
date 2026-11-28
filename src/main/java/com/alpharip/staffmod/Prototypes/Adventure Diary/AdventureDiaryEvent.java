package com.alpharip.staffmod;

/** A normalized event that can be turned into a diary entry. */
public final class AdventureDiaryEvent {
    public enum Type { WORLD_ENTERED, MOB_KILLED, PLAYER_DIED, ITEM_PICKED_UP, DIMENSION_CHANGED, BOSS_DEFEATED }

    private final Type type;
    private final String subject;
    private final String detail;
    private final long timestamp;

    public AdventureDiaryEvent(Type type, String subject, String detail, long timestamp) {
        if (type == null) throw new IllegalArgumentException("type must not be null");
        this.type = type;
        this.subject = subject == null ? "" : subject;
        this.detail = detail == null ? "" : detail;
        this.timestamp = timestamp;
    }

    public Type getType() { return type; }
    public String getSubject() { return subject; }
    public String getDetail() { return detail; }
    public long getTimestamp() { return timestamp; }
}
