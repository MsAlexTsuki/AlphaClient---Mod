package com.alpharip.staffmod.winlosetracker;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Recognizes explicit, complete Practice result announcements.
 *
 * This intentionally avoids matching a result word embedded in unrelated
 * chat (for example, a player discussing a victory streak).
 */
public final class WinLossResultDetector {
    private static final Pattern SPACE = Pattern.compile("\\s+");
    private static final Pattern FORMATTING = Pattern.compile("(?i)\\u00a7[0-9A-FK-OR]");
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

    /** Returns null unless the complete normalized message is an explicit result. */
    public static WinLossResult detect(String message) {
        if (message == null) return null;
        String value = FORMATTING.matcher(message).replaceAll("");
        value = SPACE.matcher(value.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
        if (value.isEmpty()) return null;

        boolean win = WIN.matcher(value).matches();
        boolean loss = LOSS.matcher(value).matches();
        if (win == loss) return null;
        return win ? WinLossResult.WIN : WinLossResult.LOSS;
    }
}
