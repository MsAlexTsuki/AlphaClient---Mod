package com.alpharip.staffmod.badges.playtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Converts cumulative playtime seconds into the complete badge milestone list. */
public final class PlaytimeBadgeCalculator {
    private PlaytimeBadgeCalculator() { }

    public static List<PlaytimeBadgeStatus> calculate(long totalPlaytimeSeconds) {
        long safeTotal = Math.max(0L, totalPlaytimeSeconds);
        List<PlaytimeBadgeStatus> statuses = new ArrayList<PlaytimeBadgeStatus>();
        for (PlaytimeBadgeTier tier : PlaytimeBadgeTier.values()) {
            statuses.add(new PlaytimeBadgeStatus(tier, safeTotal));
        }
        return Collections.unmodifiableList(statuses);
    }
}