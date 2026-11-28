package com.alpharip.staffmod.staffops.ideas;


/** Local marker describing an event associated with an evidence item. */
public final class EvidenceEventMarker {
    private String eventType = "";
    private String description = "";
    private long timestampMillis;

    public void set(String eventType, String description, long timestampMillis) {
        this.eventType = eventType == null ? "" : eventType;
        this.description = description == null ? "" : description;
        this.timestampMillis = timestampMillis;
    }

    public String getEventType() { return eventType; }
    public String getDescription() { return description; }
    public long getTimestampMillis() { return timestampMillis; }
}