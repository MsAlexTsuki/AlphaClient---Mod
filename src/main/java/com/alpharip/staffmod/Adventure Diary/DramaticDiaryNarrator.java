package com.alpharip.staffmod;

/** Converts game events into short, dramatic journal prose. */
public final class DramaticDiaryNarrator {
    public String narrate(AdventureDiaryEvent event) {
        if (event == null) throw new IllegalArgumentException("event must not be null");
        String subject = event.getSubject().isEmpty() ? "the unknown" : event.getSubject();
        switch (event.getType()) {
            case WORLD_ENTERED: return "A new chapter began as I entered " + subject + ".";
            case MOB_KILLED: return subject + " fell. The silence that followed felt almost deliberate.";
            case PLAYER_DIED: return "Darkness claimed me. Even this defeat will become part of my story.";
            case ITEM_PICKED_UP: return "Fortune placed " + subject + " in my hands.";
            case DIMENSION_CHANGED: return "The world twisted, and I crossed into " + subject + ".";
            case BOSS_DEFEATED: return "At last, " + subject + " was defeated. The tale will remember this day.";
            default: return "Something happened, and the pages of my story grew longer.";
        }
    }
}
