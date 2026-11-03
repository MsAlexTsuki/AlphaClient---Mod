package com.alpharip.staffmod.winlosetracker;
import java.util.Locale;
import java.util.regex.Pattern;
/** Recognizes common explicit Practice win/loss announcements. */
public final class WinLossResultDetector {
    private static final Pattern SPACE = Pattern.compile("\\s+");
    private static final Pattern WIN = Pattern.compile(".*(?:\\byou (?:have )?won\\b|\\byou win\\b|\\byou are the winner\\b|\\bvictory!?\\b|\\bwon the (?:match|game|round|duel)\\b).*");
    private static final Pattern LOSS = Pattern.compile(".*(?:\\byou (?:have )?lost\\b|\\byou lose\\b|\\byou were defeated\\b|\\byou have been defeated\\b|\\bdefeat!?\\b|\\byou have been eliminated\\b).*");
    private WinLossResultDetector() { }
    /** Returns null for neutral or ambiguous chat. */
    public static WinLossResult detect(String message) {
        if (message == null) return null;
        String value = SPACE.matcher(message.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
        if (value.isEmpty()) return null;
        boolean win = WIN.matcher(value).matches();
        boolean loss = LOSS.matcher(value).matches();
        if (win == loss) return null;
        return win ? WinLossResult.WIN : WinLossResult.LOSS;
    }
}
