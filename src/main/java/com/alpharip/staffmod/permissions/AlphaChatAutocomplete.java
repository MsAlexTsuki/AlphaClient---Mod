package com.alpharip.staffmod.permissions;

import java.util.ArrayList;
import java.util.List;

/** Filters client-side slash-command suggestions without unregistering commands. */
public final class AlphaChatAutocomplete {
    private AlphaChatAutocomplete() { }

    public static String[] filter(String[] suggestions) {
        if (suggestions == null || suggestions.length == 0) return suggestions;
        List<String> visible = new ArrayList<String>(suggestions.length);
        for (String suggestion : suggestions) {
            if (suggestion == null) continue;
            String value = suggestion.trim();
            if (!value.startsWith("/") || AlphaCommandVisibility.canShow(value)) {
                visible.add(suggestion);
            }
        }
        return visible.toArray(new String[visible.size()]);
    }
}
