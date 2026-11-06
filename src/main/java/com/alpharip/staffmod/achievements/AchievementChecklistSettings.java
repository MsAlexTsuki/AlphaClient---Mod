package com.alpharip.staffmod.achievements;

/** Session-local settings for the achievement checklist prototype. */
public final class AchievementChecklistSettings {
    private boolean enabled = true;
    private String searchQuery = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery == null ? "" : searchQuery.trim();
    }
}
