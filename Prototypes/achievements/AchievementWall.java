package com.alpharip.staffmod.achievements;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Coordinates the local achievement wall and preserves the original unlock-record API. */
public final class AchievementWall {
    private final Map<String, Long> unlocked = new LinkedHashMap<String, Long>();
    private final AchievementChecklist checklist;
    private final AchievementWallSettings settings;
    private final AchievementWallService service;

    public AchievementWall() { this(new AchievementChecklist(), new AchievementWallSettings()); }

    public AchievementWall(AchievementChecklist checklist, AchievementWallSettings settings) {
        this.checklist = checklist == null ? new AchievementChecklist() : checklist;
        this.settings = settings == null ? new AchievementWallSettings() : settings;
        this.service = new AchievementWallService(this.checklist, this.settings);
    }

    public AchievementChecklist getChecklist() { return checklist; }
    public AchievementWallSettings getSettings() { return settings; }
    public AchievementWallService getService() { return service; }

    public void unlock(String achievementId, long timestampMillis) {
        if (achievementId == null || achievementId.trim().isEmpty()) return;
        if (!unlocked.containsKey(achievementId)) unlocked.put(achievementId, Math.max(1L, timestampMillis));
        checklist.unlock(achievementId, timestampMillis);
    }

    public boolean isUnlocked(String achievementId) {
        if (achievementId == null) return false;
        AchievementEntry entry = checklist.get(achievementId);
        return unlocked.containsKey(achievementId) || (entry != null && entry.isUnlocked());
    }

    public Long getUnlockTime(String achievementId) {
        Long localTime = unlocked.get(achievementId);
        if (localTime != null) return localTime;
        AchievementEntry entry = checklist.get(achievementId);
        return entry != null && entry.isUnlocked() ? Long.valueOf(entry.getUnlockedAtMillis()) : null;
    }

    public Map<String, Long> getUnlocked() {
        Map<String, Long> snapshot = new LinkedHashMap<String, Long>(unlocked);
        for (AchievementEntry entry : checklist.getUnlocked()) {
            if (!snapshot.containsKey(entry.getId()))
                snapshot.put(entry.getId(), Long.valueOf(entry.getUnlockedAtMillis()));
        }
        return Collections.unmodifiableMap(snapshot);
    }

    public AchievementWallSnapshot createSnapshot() { return service.createSnapshot(); }
    public void clearLocalUnlockRecords() { unlocked.clear(); }
}
