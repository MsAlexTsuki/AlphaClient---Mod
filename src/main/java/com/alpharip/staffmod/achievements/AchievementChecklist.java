package com.alpharip.staffmod.achievements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory achievement registry and progress manager; no account or remote integration. */
public final class AchievementChecklist {
    private final Map<String, AchievementEntry> entries = new LinkedHashMap<String, AchievementEntry>();

    public boolean add(AchievementEntry entry) {
        if (entry == null || entries.containsKey(entry.getId())) return false;
        entries.put(entry.getId(), entry);
        return true;
    }

    public AchievementEntry get(String id) { return entries.get(id); }

    public boolean remove(String id) { return entries.remove(id) != null; }

    public boolean updateProgress(String id, int progress) {
        AchievementEntry entry = entries.get(id);
        if (entry == null) return false;
        entry.setProgress(progress);
        return true;
    }

    public boolean unlock(String id, long timestampMillis) {
        AchievementEntry entry = entries.get(id);
        if (entry == null) return false;
        entry.unlock(timestampMillis);
        return true;
    }

    public List<AchievementEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<AchievementEntry>(entries.values()));
    }

    public List<AchievementEntry> getUnlocked() {
        List<AchievementEntry> result = new ArrayList<AchievementEntry>();
        for (AchievementEntry entry : entries.values()) if (entry.isUnlocked()) result.add(entry);
        return Collections.unmodifiableList(result);
    }

    public int getCompletedCount() { return getUnlocked().size(); }
    public int getTotalCount() { return entries.size(); }
    public void clear() { entries.clear(); }
}
