package com.alpharip.staffmod.statistics.duels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable view of one opponent's duel statistics. */
public final class DuelStatisticsSnapshot {
    private final String opponentName;
    private final int wins;
    private final int losses;
    private final int currentWinStreak;
    private final int bestWinStreak;
    private final double winRate;
    private final List<DuelRecord> history;

    public DuelStatisticsSnapshot(String opponentName, int wins, int losses,
            int currentWinStreak, int bestWinStreak, double winRate, List<DuelRecord> history) {
        this.opponentName = opponentName;
        this.wins = wins;
        this.losses = losses;
        this.currentWinStreak = currentWinStreak;
        this.bestWinStreak = bestWinStreak;
        this.winRate = winRate;
        this.history = Collections.unmodifiableList(new ArrayList<DuelRecord>(history));
    }

    public String getOpponentName() { return opponentName; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getGamesPlayed() { return wins + losses; }
    public int getCurrentWinStreak() { return currentWinStreak; }
    public int getBestWinStreak() { return bestWinStreak; }
    public double getWinRate() { return winRate; }
    public List<DuelRecord> getHistory() { return history; }
}
