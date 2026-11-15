package com.alpharip.staffmod.AlphaCoins.alphacoinsplaytime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Defines the recurring playtime rewards and one-time playtime milestones. */
public enum PlaytimeRewardDefinition {
    DAILY("daily", "Every 24 hours", 24L * 60L * 60L * 1000L, 5L, true),
    TEN_DAYS("ten_days", "Every 10 days", 10L * 24L * 60L * 60L * 1000L, 50L, true),
    ONE_MONTH("one_month", "1 month", 30L * 24L * 60L * 60L * 1000L, 100L, false),
    THREE_MONTHS("three_months", "3 months", 90L * 24L * 60L * 60L * 1000L, 300L, false),
    SIX_MONTHS("six_months", "6 months", 180L * 24L * 60L * 60L * 1000L, 800L, false),
    NINE_MONTHS("nine_months", "9 months", 270L * 24L * 60L * 60L * 1000L, 1200L, false),
    ONE_YEAR("one_year", "1 year", 365L * 24L * 60L * 60L * 1000L, 2400L, false);

    private static final List<PlaytimeRewardDefinition> ALL =
            Collections.unmodifiableList(Arrays.asList(values()));

    private final String id;
    private final String title;
    private final long thresholdMillis;
    private final long rewardCoins;
    private final boolean recurring;

    PlaytimeRewardDefinition(String id, String title, long thresholdMillis,
                             long rewardCoins, boolean recurring) {
        this.id = id;
        this.title = title;
        this.thresholdMillis = thresholdMillis;
        this.rewardCoins = rewardCoins;
        this.recurring = recurring;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public long getThresholdMillis() { return thresholdMillis; }
    public long getRewardCoins() { return rewardCoins; }
    public boolean isRecurring() { return recurring; }

    public static List<PlaytimeRewardDefinition> getAll() { return ALL; }

    public static PlaytimeRewardDefinition find(String id) {
        if (id == null) return null;
        for (PlaytimeRewardDefinition definition : values()) {
            if (definition.id.equals(id)) return definition;
        }
        return null;
    }
}
