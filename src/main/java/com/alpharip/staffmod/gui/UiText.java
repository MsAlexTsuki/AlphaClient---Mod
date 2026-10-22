package com.alpharip.staffmod.gui;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Normalizes all-uppercase UI labels while preserving the CPS and FPS acronyms. */
public final class UiText {
    private static final Pattern UPPERCASE_WORD = Pattern.compile("\\b[A-Z][A-Z0-9]*(?:[-'][A-Z0-9]+)*\\b");

    private UiText() {}

    public static String normalize(String text) {
        if (text == null || text.isEmpty()) return text;
        Matcher matcher = UPPERCASE_WORD.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String word = matcher.group();
            String replacement;
            if ("CPS".equals(word) || "FPS".equals(word)) {
                replacement = word;
            } else {
                String lower = word.toLowerCase(Locale.ROOT);
                replacement = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
