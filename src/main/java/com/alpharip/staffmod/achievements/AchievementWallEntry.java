package com.alpharip.staffmod.achievements;

/** Immutable display snapshot for one achievement on the profile wall. */
public final class AchievementWallEntry {
    private final String id, title, description, category;
    private final int progress, target;
    private final long unlockedAtMillis;
    private final boolean unlocked;

    public AchievementWallEntry(AchievementEntry source) {
        if (source == null) throw new IllegalArgumentException("Achievement source is required");
        id = source.getId();
        title = source.getTitle();
        description = source.getDescription();
        category = source.getCategory();
        progress = source.getProgress();
        target = source.getTarget();
        unlockedAtMillis = source.getUnlockedAtMillis();
        unlocked = source.isUnlocked();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public int getProgress() { return progress; }
    public int getTarget() { return target; }
    public long getUnlockedAtMillis() { return unlockedAtMillis; }
    public boolean isUnlocked() { return unlocked; }
    public int getProgressPercent() {
        return target < 1 ? 0 : Math.max(0, Math.min(100, (int) ((long) progress * 100L / target)));
    }
}
