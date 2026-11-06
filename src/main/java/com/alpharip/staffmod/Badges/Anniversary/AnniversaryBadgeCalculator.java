package com.alpharip.staffmod.badges.anniversary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Calculates earned account-age milestones using calendar months, not fixed day counts. */
public final class AnniversaryBadgeCalculator {
    private AnniversaryBadgeCalculator() { }

    public static List<AnniversaryBadgeStatus> calculate(Date accountCreatedAt, Date referenceDate) {
        if (accountCreatedAt == null || referenceDate == null) {
            return Collections.emptyList();
        }

        List<AnniversaryBadgeStatus> result = new ArrayList<AnniversaryBadgeStatus>();
        for (AnniversaryBadgeTier tier : AnniversaryBadgeTier.values()) {
            Calendar milestone = Calendar.getInstance();
            milestone.setTime(accountCreatedAt);
            milestone.add(Calendar.MONTH, tier.getMonthsRequired());

            Date earnedAt = milestone.getTime();
            boolean earned = !earnedAt.after(referenceDate);
            result.add(new AnniversaryBadgeStatus(tier, earned, earned ? earnedAt : null));
        }
        return Collections.unmodifiableList(result);
    }
}
