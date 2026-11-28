package com.alpharip.staffmod.staffops.ideas;

import java.time.LocalDate;

/**
 * Tracks the best kill streak recorded for each calendar day.
 * Game event hooks and persistence must be connected by client integration.
 */
public final class DailyBestKillStreak {
    private LocalDate day;
    private int currentStreak;
    private int bestStreak;

    public void startDay(LocalDate date) {
        if (date == null) {
            return;
        }
        if (!date.equals(day)) {
            day = date;
            currentStreak = 0;
            bestStreak = 0;
        }
    }

    public int recordKill() {
        currentStreak++;
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak;
        }
        return currentStreak;
    }

    public void recordDeath() {
        currentStreak = 0;
    }

    public LocalDate getDay() {
        return day;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }
}
