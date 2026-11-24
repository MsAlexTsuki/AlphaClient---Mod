package com.alpharip.staffmod;

/** A dated, narrated diary entry. */
public final class AdventureDiaryEntry {
    private final AdventureDiaryEvent sourceEvent;
    private final String narrative;

    public AdventureDiaryEntry(AdventureDiaryEvent sourceEvent, String narrative) {
        if (sourceEvent == null) throw new IllegalArgumentException("sourceEvent must not be null");
        this.sourceEvent = sourceEvent;
        this.narrative = narrative == null ? "" : narrative;
    }

    public AdventureDiaryEvent getSourceEvent() { return sourceEvent; }
    public String getNarrative() { return narrative; }
    public long getTimestamp() { return sourceEvent.getTimestamp(); }
}
