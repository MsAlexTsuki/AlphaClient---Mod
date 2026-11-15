package com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges;

public enum ChallengeDifficulty {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    private final String displayName;

    ChallengeDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
