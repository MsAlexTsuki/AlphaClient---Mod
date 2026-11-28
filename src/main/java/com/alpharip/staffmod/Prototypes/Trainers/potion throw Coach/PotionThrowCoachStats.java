package com.alpharip.staffmod.trainers.potionthrowcoach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Calculates summary values from the attempts currently retained by the coach. */
public final class PotionThrowCoachStats {
    private final List<PotionThrowResult> results;

    PotionThrowCoachStats(List<PotionThrowResult> results) {
        this.results = Collections.unmodifiableList(
                new ArrayList<PotionThrowResult>(results));
    }

    public int getCompletedThrows() { return results.size(); }

    public int getAccurateThrows() {
        int count = 0;
        for (PotionThrowResult result : results) {
            if (result.isAccurate()) count++;
        }
        return count;
    }

    public double getAccuracyPercent() {
        return results.isEmpty() ? 0.0D
                : getAccurateThrows() * 100.0D / results.size();
    }

    /** Average reaction time excludes attempts without a preceding damage event. */
    public double getAverageReactionTimeMillis() {
        long total = 0L;
        int count = 0;
        for (PotionThrowResult result : results) {
            if (result.getReactionTimeMillis() >= 0L) {
                total += result.getReactionTimeMillis();
                count++;
            }
        }
        return count == 0 ? 0.0D : (double) total / count;
    }

    public double getAverageLandingOffset() {
        if (results.isEmpty()) return 0.0D;
        double total = 0.0D;
        for (PotionThrowResult result : results) {
            total += result.getLandingOffset();
        }
        return total / results.size();
    }

    public List<PotionThrowResult> getResults() { return results; }
}
