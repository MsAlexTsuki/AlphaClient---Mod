package com.alpharip.staffmod.winlosetracker;

/** Calculated win/loss totals for an overall server profile or one address. */
public final class WinLossStatistics {
    private final int wins;
    private final int losses;

    public WinLossStatistics(int wins, int losses) {
        this.wins = Math.max(0, wins);
        this.losses = Math.max(0, losses);
    }

    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getTotalMatches() { return wins + losses; }

    public double getWinRate() {
        int total = getTotalMatches();
        return total == 0 ? 0.0D : (100.0D * wins) / total;
    }
}
