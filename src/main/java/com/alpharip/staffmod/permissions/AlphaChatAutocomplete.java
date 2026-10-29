package com.alpharip.staffmod.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Filters only AlphaClient-side command suggestions; leaves normal commands untouched. */
public final class AlphaChatAutocomplete {
    private static final Set<String> CLIENT_COMMANDS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    "fpshud", "afktimer", "alphaclient:afktimer", "modules", "alphaclient:modules",
                    "fullbright", "alphaclient:fullbright", "playtime", "alphaclient:playtime",
                    "help", "alphaclient:help", "alphahelp", "hudeditor", "fps", "alphaclient:fps",
                    "legacypvp", "alphaclient:legacypvp", "profile", "profiles", "quality",
                    "settings", "alphaclient:settings", "cps", "alphaclient:cps", "general",
                    "sessiontimer", "alphaclient:sessiontimer", "nameviewer", "autoreconnect",
                    "cpshud", "autogg", "module", "alphaclient:module", "alphaclient", "pothud",
                    "bhop", "staffgeneral", "stafftmod", "staffmods", "staffmod", "staffsrmod",
                    "staffadmin", "staffsradmin", "staffplatformadmin", "staffplatadmin",
                    "staffmanagement", "staffmanager", "staffoperations", "alphanote",
                    "staffowner", "stafffounder", "alphaperms", "wtap", "scout", "donator",
                    "rogue", "elite", "alpha", "alphaplus", "beta", "pixelgarden", "garden",
                    "pgivecoins"
            )));

    private AlphaChatAutocomplete() { }

    public static String[] filter(String[] suggestions) {
        if (suggestions == null || suggestions.length == 0) return suggestions;
        List<String> visible = new ArrayList<String>(suggestions.length);
        for (String suggestion : suggestions) {
            if (suggestion == null || suggestion.trim().isEmpty()) continue;
            String value = suggestion.trim().toLowerCase(Locale.ROOT);
            String candidate = value.startsWith("/") ? value.substring(1) : value;
            String root = candidate.split("\\s+", 2)[0];

            // Unknown commands are normal Minecraft/server commands: preserve them.
            // Only apply rank rules to the explicitly identified AlphaClient commands.
            if (!CLIENT_COMMANDS.contains(root) || AlphaCommandVisibility.canShow("/" + candidate)) {
                visible.add(suggestion);
            }
        }
        return visible.toArray(new String[visible.size()]);
    }
}
