package com.alpharip.staffmod.achievements;

/** One achievement definition and its locally tracked progress. */
public final class AchievementEntry {
    private final String id;
    private final String title;
    private final String description;
    private final String explanation;
    private final String category;
    private final int target;
    private int progress;
    private long unlockedAtMillis;

    public AchievementEntry(String id, String title, String description, String explanation,
                            String category, int target) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Achievement id is required");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Achievement title is required");
        if (target < 1) throw new IllegalArgumentException("Target must be at least 1");
        this.id = id.trim();
        this.title = title.trim();
        this.description = description == null ? "" : description.trim();
        this.explanation = explanation == null ? "" : explanation.trim();
        this.category = category == null || category.trim().isEmpty() ? "General" : category.trim();
        this.target = target;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getExplanation() { return explanation; }
    public String getCategory() { return category; }
    public int getTarget() { return target; }
    public int getProgress() { return progress; }
    public long getUnlockedAtMillis() { return unlockedAtMillis; }
    public boolean isUnlocked() { return unlockedAtMillis > 0L || progress >= target; }
    public int getProgressPercent() { return Math.min(100, (int) ((long) progress * 100L / target)); }

    public void setProgress(int value) {
        progress = Math.max(0, Math.min(target, value));
        if (progress >= target && unlockedAtMillis == 0L) unlockedAtMillis = System.currentTimeMillis();
    }

    public void unlock(long timestampMillis) {
        progress = target;
        if (unlockedAtMillis == 0L) unlockedAtMillis = Math.max(1L, timestampMillis);
    }
}
