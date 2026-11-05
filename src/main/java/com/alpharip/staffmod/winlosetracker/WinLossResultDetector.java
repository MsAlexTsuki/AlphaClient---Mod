// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

import java.util.Locale;
import java.util.regex.Pattern;

/** Detects complete, explicit result announcements without matching ordinary chat. */
public final class WinLossResultDetector {
    private static final Pattern FORMATTING = Pattern.compile("(?i)\\u00a7[0-9A-FK-OR]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern MATCH_RESULT = Pattern.compile("winner:\\s*([a-zA-Z0-9_]{1,16})[^a-zA-Z0-9_\\r\\n]{1,16}loser:\\s*([a-zA-Z0-9_]{1,16})", Pattern.CASE_INSENSITIVE);
    private static final Pattern WIN = Pattern.compile(
            "^(?:(?:match|game|round|duel) result:\\s*)?"
            + "(?:victory!?|you (?:have )?won(?: the (?:match|game|round|duel))!?|"
            + "you win!?|you are the winner!?)$");
    private static final Pattern LOSS = Pattern.compile(
            "^(?:(?:match|game|round|duel) result:\\s*)?"
            + "(?:defeat!?|you (?:have )?lost(?: the (?:match|game|round|duel))!?|"
            + "you lose!?|you were defeated!?|you have been defeated!?|"
            + "you have been eliminated!?)$");

    private WinLossResultDetector() { }

    /** Detects a match announcement that identifies the local player. */
    public static WinLossResult detect(String message, String playerName) {
        if (message != null && playerName != null && !playerName.trim().isEmpty()) {
            String cleanedMessage = FORMATTING.matcher(message).replaceAll("");
            cleanedMessage = cleanedMessage.replace('\u00a0', ' ');
            java.util.regex.Matcher match = MATCH_RESULT.matcher(cleanedMessage);
            while (match.find()) {
                String winner = match.group(1).trim();
                String loser = match.group(2).trim();
                if (winner.equalsIgnoreCase(playerName.trim())) return WinLossResult.WIN;
                if (loser.equalsIgnoreCase(playerName.trim())) return WinLossResult.LOSS;
            }
        }
        return detect(message);
    }

    /** Returns null unless the entire cleaned message is a recognized result. */
    public static WinLossResult detect(String message) {
        if (message == null) return null;
        String cleaned = FORMATTING.matcher(message).replaceAll("");
        cleaned = WHITESPACE.matcher(cleaned.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
        if (cleaned.isEmpty()) return null;

        boolean win = WIN.matcher(cleaned).matches();
        boolean loss = LOSS.matcher(cleaned).matches();
        if (win == loss) return null;
        return win ? WinLossResult.WIN : WinLossResult.LOSS;
    }
}
