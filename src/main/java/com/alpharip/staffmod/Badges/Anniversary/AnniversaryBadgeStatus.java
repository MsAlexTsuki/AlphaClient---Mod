package com.alpharip.staffmod.badges.anniversary;

import java.util.Date;

/** Result for one anniversary milestone. */
public final class AnniversaryBadgeStatus {
    private final AnniversaryBadgeTier tier;
    private final boolean earned;
    private final Date earnedAt;

    public AnniversaryBadgeStatus(AnniversaryBadgeTier tier, boolean earned, Date earnedAt) {
        if (tier == null) throw new IllegalArgumentException("Badge tier is required.");
        this.tier = tier;
        this.earned = earned;
        this.earnedAt = earnedAt == null ? null : new Date(earnedAt.getTime());
    }

    public AnniversaryBadgeTier getTier() { return tier; }
    public boolean isEarned() { return earned; }
    public Date getEarnedAt() { return earnedAt == null ? null : new Date(earnedAt.getTime()); }
}
