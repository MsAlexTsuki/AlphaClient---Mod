package com.alpharip.staffmod.achievements.suffix;

/** Session-local suffix display preference and selected suffix identifier. */
public final class AchievementSuffixSettings {
    private boolean enabled = true;
    private String selectedSuffixId;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getSelectedSuffixId() { return selectedSuffixId; }

    public void setSelectedSuffixId(String selectedSuffixId) {
        this.selectedSuffixId = selectedSuffixId == null || selectedSuffixId.trim().isEmpty()
                ? null : selectedSuffixId.trim();
    }

    public void clearSelection() { selectedSuffixId = null; }
}
