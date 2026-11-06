package com.alpharip.staffmod.achievements;

/** Session-local display settings for the achievement wall prototype. */
public final class AchievementWallSettings {
    private boolean enabled = true;
    private boolean showLocked;
    private AchievementWallSort sort = AchievementWallSort.RECENT_FIRST;
    private String searchQuery = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isShowLocked() { return showLocked; }
    public void setShowLocked(boolean showLocked) { this.showLocked = showLocked; }
    public AchievementWallSort getSort() { return sort; }
    public void setSort(AchievementWallSort sort) {
        this.sort = sort == null ? AchievementWallSort.RECENT_FIRST : sort;
    }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String query) { searchQuery = query == null ? "" : query.trim(); }
}
