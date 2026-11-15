package com.alpharip.staffmod.AlphaDailyChallenges;

/** Immutable definition for one repeatable daily objective. */
public final class DailyChallengeDefinition {
    private final String id;
    private final String title;
    private final String description;
    private final ChallengeMetric metric;
    private final int target;
    private final int reward;
    private final ChallengeDifficulty difficulty;

    public DailyChallengeDefinition(String id, String title, String description,
                                    ChallengeMetric metric, int target, int reward,
                                    ChallengeDifficulty difficulty) {
        if (id == null || id.trim().isEmpty() || title == null || title.trim().isEmpty()
                || metric == null || difficulty == null || target <= 0 || reward <= 0) {
            throw new IllegalArgumentException("A valid challenge definition is required.");
        }
        this.id = id;
        this.title = title;
        this.description = description == null ? "" : description;
        this.metric = metric;
        this.target = target;
        this.reward = reward;
        this.difficulty = difficulty;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ChallengeMetric getMetric() { return metric; }
    public int getTarget() { return target; }
    public int getReward() { return reward; }
    public ChallengeDifficulty getDifficulty() { return difficulty; }
}
