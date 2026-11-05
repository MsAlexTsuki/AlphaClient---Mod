// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

import java.util.Locale;
import java.util.regex.Pattern;

/** Detects complete, explicit result announcements without matching ordinary chat. */
public final class WinLossResultDetector {
    private static final Pattern FORMATTING = Pattern.compile("(?i)\\u00a7[0-9A-FK-OR]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
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
