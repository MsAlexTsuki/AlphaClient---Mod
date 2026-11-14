package com.alpharip.staffmod.autosave;

/** Formats elapsed time for the singleplayer-only AutoSave indicator. */
public final class AutoSaveFormatter {
    private AutoSaveFormatter() { }

    public static String formatElapsed(long seconds) {
        if (seconds == Long.MAX_VALUE || seconds < 0L) return "No save detected";
        if (seconds == 0L) return "just now";
        if (seconds == 1L) return "1 second ago";
        return seconds + " seconds ago";
    }

    public static String createHudText(AutoSaveState state, long nowMillis) {
        if (state == null) return "";
        return "World saved " + formatElapsed(state.getSecondsSinceSave(nowMillis));
    }
}
