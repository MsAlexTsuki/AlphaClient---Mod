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
            if (value.isEmpty()) continue;

            // GuiChat/Forge may provide either "/command" or "command".
            // Filtering only slash-prefixed entries allowed bare command names
            // to bypass the rank visibility rules entirely.
            String commandCandidate = value.startsWith("/") ? value : "/" + value;
            if (AlphaCommandVisibility.canShow(commandCandidate)) {
                visible.add(suggestion);
            }
        }
        return visible.toArray(new String[visible.size()]);
    }
}
