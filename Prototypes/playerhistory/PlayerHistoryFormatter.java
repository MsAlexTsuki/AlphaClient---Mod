package com.alpharip.staffmod.playerhistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Formatting helpers for the standalone history screen. */
public final class PlayerHistoryFormatter {
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private PlayerHistoryFormatter() { }

    public static String dateTime(long timestamp) {
        if (timestamp <= 0L) return "Unknown";
        return new SimpleDateFormat(DATE_PATTERN, Locale.ROOT).format(new Date(timestamp));
    }

    public static String duration(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;
        if (days > 0L) return days + "d " + hours + "h";
        if (hours > 0L) return hours + "h " + minutes + "m";
        if (minutes > 0L) return minutes + "m " + remainingSeconds + "s";
        return remainingSeconds + "s";
    }
}
