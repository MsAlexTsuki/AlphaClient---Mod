package com.alpharip.staffmod.statistics.duels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tracks local duel statistics for one opponent. */
public class OpponentSpecificDuelStatistics {
    public static final String NAME = "Opponent-Specific Duel Statistics";
    public static final String CATEGORY = "Statistics";
    public static final boolean REQUIRES_SUPABASE = false;
    public static final boolean REQUIRES_SERVER_SUPPORT = false;

    private String opponentName = "";
    private int wins;
    private int losses;
    private int currentWinStreak;
    private int bestWinStreak;
    private final List<DuelRecord> history = new ArrayList<DuelRecord>();
    private DuelStatisticsConfig config;

    public OpponentSpecificDuelStatistics() {
        this(DuelStatisticsConfig.defaults());
    }

    public OpponentSpecificDuelStatistics(DuelStatisticsConfig config) {
        if (config == null) throw new IllegalArgumentException("config cannot be null");
        this.config = config;
    }

    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String value) { opponentName = value == null ? "" : value.trim(); }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getGamesPlayed() { return wins + losses; }
    public int getCurrentWinStreak() { return currentWinStreak; }
    public int getBestWinStreak() { return bestWinStreak; }

    public double getWinRate() {
        int games = getGamesPlayed();
        return games == 0 ? 0.0D : (wins * 100.0D) / games;
    }

    public void recordWin() { recordWin(System.currentTimeMillis()); }
    public void recordLoss() { recordLoss(System.currentTimeMillis()); }

    public void recordWin(long timestamp) {
        wins++;
        currentWinStreak++;
        if (currentWinStreak > bestWinStreak) bestWinStreak = currentWinStreak;
        addRecord(new DuelRecord(true, timestamp));
    }

    public void recordLoss(long timestamp) {
        losses++;
        currentWinStreak = 0;
        addRecord(new DuelRecord(false, timestamp));
    }

    private void addRecord(DuelRecord record) {
        if (config.isHistoryEnabled()) {
            history.add(record);
            while (history.size() > config.getMaxHistoryEntries()) history.remove(0);
        }
    }

    public List<DuelRecord> getHistory() {
        return Collections.unmodifiableList(new ArrayList<DuelRecord>(history));
    }

    public DuelStatisticsSnapshot snapshot() {
        return new DuelStatisticsSnapshot(opponentName, wins, losses,
                currentWinStreak, bestWinStreak, getWinRate(), history);
    }

    public void clearStatistics() {
        wins = 0;
        losses = 0;
        currentWinStreak = 0;
        bestWinStreak = 0;
        history.clear();
    }

    public DuelStatisticsConfig getConfig() { return config; }
    public void setConfig(DuelStatisticsConfig config) {
        if (config == null) throw new IllegalArgumentException("config cannot be null");
        this.config = config;
        while (history.size() > config.getMaxHistoryEntries()) history.remove(0);
        if (!config.isHistoryEnabled()) history.clear();
    }
}
