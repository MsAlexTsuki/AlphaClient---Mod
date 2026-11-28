package com.alpharip.staffmod.achievements.suffix;

/** Definition of one display suffix and the achievement required to unlock it. */
public final class AchievementSuffixEntry {
    private final String id;
    private final String requiredAchievementId;
    private final String displayText;
    private final String description;

    public AchievementSuffixEntry(String id, String requiredAchievementId,
                                   String displayText, String description) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Suffix id is required");
        if (requiredAchievementId == null || requiredAchievementId.trim().isEmpty()) {
            throw new IllegalArgumentException("Required achievement id is required");
        }
        if (displayText == null || displayText.trim().isEmpty()) {
            throw new IllegalArgumentException("Suffix display text is required");
        }
        this.id = id.trim();
        this.requiredAchievementId = requiredAchievementId.trim();
        this.displayText = displayText.trim();
        this.description = description == null ? "" : description.trim();
    }

    public String getId() { return id; }
    public String getRequiredAchievementId() { return requiredAchievementId; }
    public String getDisplayText() { return displayText; }
    public String getDescription() { return description; }
}
