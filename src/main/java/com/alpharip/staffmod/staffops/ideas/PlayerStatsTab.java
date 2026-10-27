package com.alpharip.staffmod.staffops.ideas;

/**
 * Data model for optional player statistics in the tab list.
 * Stats collection, server/API access, and tab-list rendering are not connected yet.
 */
public final class PlayerStatsTab {
    public static final String FEATURE_NAME = "Player Stats in Tab";

    public static final class Stats {
        private final String playerName;
        private final int level;
        private final int wins;
        private final int kills;
        private final double winRate;

        public Stats(String playerName, int level, int wins, int kills, double winRate) {
            this.playerName = playerName == null ? "" : playerName.trim();
            this.level = Math.max(0, level);
            this.wins = Math.max(0, wins);
            this.kills = Math.max(0, kills);
            this.winRate = Double.isNaN(winRate) || Double.isInfinite(winRate)
                    ? 0.0D : Math.max(0.0D, Math.min(100.0D, winRate));
        }

        public String getPlayerName() { return playerName; }
        public int getLevel() { return level; }
        public int getWins() { return wins; }
        public int getKills() { return kills; }
        public double getWinRate() { return winRate; }

        public String toTabText() {
            return playerName + "  " + level + "★  W:" + wins + "  K:" + kills
                    + "  WR:" + String.format(java.util.Locale.ROOT, "%.1f%%", winRate);
        }
    }

    private boolean enabled;
    private boolean showLevel = true;
    private boolean showWins = true;
    private boolean showKills = true;
    private boolean showWinRate;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public boolean isShowLevel() { return showLevel; }
    public void setShowLevel(boolean value) { showLevel = value; }
    public boolean isShowWins() { return showWins; }
    public void setShowWins(boolean value) { showWins = value; }
    public boolean isShowKills() { return showKills; }
    public void setShowKills(boolean value) { showKills = value; }
    public boolean isShowWinRate() { return showWinRate; }
    public void setShowWinRate(boolean value) { showWinRate = value; }
}
