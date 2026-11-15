package com.alpharip.staffmod.AlphaDailyChallenges;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Deterministic daily rotation. Every player receives the same set on a given date. */
public final class DailyChallengeCatalog {
    private static final DailyChallengeDefinition[] PLAYTIME = {
            definition("play_10", "Warm Up", "Play for 10 minutes.", ChallengeMetric.PLAY_MINUTES, 10, 10, ChallengeDifficulty.EASY),
            definition("play_20", "Stay Awhile", "Play for 20 minutes.", ChallengeMetric.PLAY_MINUTES, 20, 20, ChallengeDifficulty.MEDIUM),
            definition("play_30", "Dedicated Session", "Play for 30 minutes.", ChallengeMetric.PLAY_MINUTES, 30, 30, ChallengeDifficulty.HARD)
    };
    private static final DailyChallengeDefinition[] BLOCKS = {
            definition("blocks_20", "Getting Busy", "Break 20 blocks.", ChallengeMetric.BLOCKS_BROKEN, 20, 10, ChallengeDifficulty.EASY),
            definition("blocks_40", "Block Breaker", "Break 40 blocks.", ChallengeMetric.BLOCKS_BROKEN, 40, 20, ChallengeDifficulty.MEDIUM),
            definition("blocks_64", "Resource Run", "Break 64 blocks.", ChallengeMetric.BLOCKS_BROKEN, 64, 30, ChallengeDifficulty.HARD)
    };
    private static final DailyChallengeDefinition[] CRAFTING = {
            definition("craft_3", "First Creations", "Craft 3 items.", ChallengeMetric.ITEMS_CRAFTED, 3, 10, ChallengeDifficulty.EASY),
            definition("craft_8", "Busy Hands", "Craft 8 items.", ChallengeMetric.ITEMS_CRAFTED, 8, 20, ChallengeDifficulty.MEDIUM),
            definition("craft_15", "Master Crafter", "Craft 15 items.", ChallengeMetric.ITEMS_CRAFTED, 15, 30, ChallengeDifficulty.HARD)
    };
    private static final DailyChallengeDefinition[] COMBAT = {
            definition("mobs_2", "Monster Hunter", "Defeat 2 hostile mobs.", ChallengeMetric.HOSTILE_MOBS_KILLED, 2, 15, ChallengeDifficulty.EASY),
            definition("mobs_5", "Mob Control", "Defeat 5 hostile mobs.", ChallengeMetric.HOSTILE_MOBS_KILLED, 5, 25, ChallengeDifficulty.MEDIUM),
            definition("mobs_8", "Night Shift", "Defeat 8 hostile mobs.", ChallengeMetric.HOSTILE_MOBS_KILLED, 8, 35, ChallengeDifficulty.HARD)
    };

    private DailyChallengeCatalog() { }

    public static List<DailyChallengeDefinition> forDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date is required.");
        long day = date.toEpochDay();
        List<DailyChallengeDefinition> result = new ArrayList<DailyChallengeDefinition>(4);
        result.add(PLAYTIME[(int) Math.floorMod(day, PLAYTIME.length)]);
        result.add(BLOCKS[(int) Math.floorMod(day + 1L, BLOCKS.length)]);
        result.add(CRAFTING[(int) Math.floorMod(day + 2L, CRAFTING.length)]);
        result.add(COMBAT[(int) Math.floorMod(day + 1L, COMBAT.length)]);
        return Collections.unmodifiableList(result);
    }

    private static DailyChallengeDefinition definition(String id, String title, String description,
                                                       ChallengeMetric metric, int target, int reward,
                                                       ChallengeDifficulty difficulty) {
        return new DailyChallengeDefinition(id, title, description, metric, target, reward, difficulty);
    }
}
