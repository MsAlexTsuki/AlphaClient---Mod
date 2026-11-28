package com.alpharip.staffmod.achievements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable result prepared for the profile-wall GUI. */
public final class AchievementWallSnapshot {
    private final List<AchievementWallEntry> entries;
    private final int totalCount, unlockedCount, lockedCount;

    public AchievementWallSnapshot(List<AchievementWallEntry> entries, int totalCount,
                                   int unlockedCount, int lockedCount) {
        this.entries = Collections.unmodifiableList(new ArrayList<AchievementWallEntry>(
                entries == null ? Collections.<AchievementWallEntry>emptyList() : entries));
        this.totalCount = Math.max(0, totalCount);
        this.unlockedCount = Math.max(0, unlockedCount);
        this.lockedCount = Math.max(0, lockedCount);
    }

    public List<AchievementWallEntry> getEntries() { return entries; }
    public int getTotalCount() { return totalCount; }
    public int getUnlockedCount() { return unlockedCount; }
    public int getLockedCount() { return lockedCount; }
    public int getCompletionPercent() {
        return totalCount == 0 ? 0 : Math.min(100, unlockedCount * 100 / totalCount);
    }
}
