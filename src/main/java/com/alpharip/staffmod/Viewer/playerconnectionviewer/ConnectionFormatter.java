package com.alpharip.staffmod.Viewer.playerconnectionviewer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Formatting helpers for the Player Connection Viewer screen. */
public final class ConnectionFormatter {
    private ConnectionFormatter() { }

    public static String duration(long durationMillis) {
        long totalSeconds = Math.max(0L, durationMillis) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) {
            return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    public static String timestamp(long timestampMillis) {
        if (timestampMillis <= 0L) return "Unknown";
        return new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
                .format(new Date(timestampMillis));
    }
}
