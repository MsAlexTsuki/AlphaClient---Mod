package com.alpharip.staffmod.AlphaCoins.AlphaCoinsDailyChallenges;

/** Mutable progress for one daily challenge. */
public final class DailyChallengeProgress {
    private final DailyChallengeDefinition definition;
    private int progress;
    private boolean claimed;

    public DailyChallengeProgress(DailyChallengeDefinition definition) {
        if (definition == null) throw new IllegalArgumentException("Definition is required.");
        this.definition = definition;
    }

    public DailyChallengeDefinition getDefinition() { return definition; }
    public synchronized int getProgress() { return progress; }
    public synchronized boolean isComplete() { return progress >= definition.getTarget(); }
    public synchronized boolean isClaimed() { return claimed; }

    public synchronized boolean addProgress(int amount) {
        if (amount <= 0 || isComplete()) return false;
        int next = progress > Integer.MAX_VALUE - amount ? Integer.MAX_VALUE : progress + amount;
        progress = Math.min(definition.getTarget(), next);
        return true;
    }

    public synchronized void restore(int savedProgress, boolean savedClaimed) {
        progress = Math.max(0, Math.min(definition.getTarget(), savedProgress));
        claimed = savedClaimed && progress >= definition.getTarget();
    }

    public synchronized boolean markClaimed() {
        if (!isComplete() || claimed) return false;
        claimed = true;
        return true;
    }
}
