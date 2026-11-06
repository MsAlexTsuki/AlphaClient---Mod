package com.alpharip.staffmod.achievements.suffix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory catalogue of suffix definitions supplied by the caller. */
public final class AchievementSuffixRegistry {
    private final Map<String, AchievementSuffixEntry> entries =
            new LinkedHashMap<String, AchievementSuffixEntry>();

    public boolean register(AchievementSuffixEntry entry) {
        if (entry == null || entries.containsKey(entry.getId())) return false;
        entries.put(entry.getId(), entry);
        return true;
    }

    public AchievementSuffixEntry get(String id) { return entries.get(id); }

    public List<AchievementSuffixEntry> getAll() {
        return Collections.unmodifiableList(new ArrayList<AchievementSuffixEntry>(entries.values()));
    }

    public boolean remove(String id) { return entries.remove(id) != null; }
    public int size() { return entries.size(); }
    public void clear() { entries.clear(); }
}
