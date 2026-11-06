package com.alpharip.staffmod.achievements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Case-insensitive search across achievement titles, descriptions, explanations, and categories. */
public final class AchievementChecklistSearch {
    private AchievementChecklistSearch() { }

    public static List<AchievementEntry> search(List<AchievementEntry> entries, String query) {
        if (entries == null || entries.isEmpty()) return Collections.emptyList();
        String needle = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);
        if (needle.isEmpty()) return Collections.unmodifiableList(new ArrayList<AchievementEntry>(entries));
        List<AchievementEntry> matches = new ArrayList<AchievementEntry>();
        for (AchievementEntry entry : entries) {
            if (entry == null) continue;
            if (contains(entry.getTitle(), needle) || contains(entry.getDescription(), needle)
                    || contains(entry.getExplanation(), needle) || contains(entry.getCategory(), needle)) {
                matches.add(entry);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(java.util.Locale.ROOT).contains(needle);
    }
}
