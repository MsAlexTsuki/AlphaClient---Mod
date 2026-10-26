package com.alpharip.staffmod.staffops.ideas;

import java.util.regex.Pattern;

/** Foundation logic for removing common sensitive values from text before clipboard use. */
public final class ClipboardSanitizer {
    public static final String FEATURE_NAME="Clipboard Sanitizer", CATEGORY="Security", STATUS="NEW";
    private static final Pattern EMAIL=Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern IPV4=Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern TOKEN=Pattern.compile("(?i)\\b(?:token|password|secret|apikey|api_key)\\s*[:=]\\s*[^\\s,;]+");
    public String sanitize(String input) {
        if(input==null) return "";
        String value=TOKEN.matcher(input).replaceAll("$0".replace("$0","[REDACTED]"));
        value=EMAIL.matcher(value).replaceAll("[EMAIL]");
        return IPV4.matcher(value).replaceAll("[IP]");
    }
}