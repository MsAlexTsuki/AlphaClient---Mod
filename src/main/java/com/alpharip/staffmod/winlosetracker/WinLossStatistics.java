// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

/** Immutable win/loss totals for a profile or one server address. */
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
        return total == 0 ? 0.0D : (wins * 100.0D) / total;
    }
}
