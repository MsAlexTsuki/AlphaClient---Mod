package com.alpharip.staffmod.aimpatternlibrary;

/** Session-local presentation and selection settings for the prototype. */
public final class AimPatternSettings {
    private String selectedPatternId;
    private AimPatternCategory filter = null;
    private boolean showStatistics = true;

    public String getSelectedPatternId() { return selectedPatternId; }
    public void setSelectedPatternId(String id) { selectedPatternId = id; }
    public AimPatternCategory getFilter() { return filter; }
    public void setFilter(AimPatternCategory filter) { this.filter = filter; }
    public boolean isShowStatistics() { return showStatistics; }
    public void setShowStatistics(boolean showStatistics) { this.showStatistics = showStatistics; }
    public void reset() {
        selectedPatternId = null;
        filter = null;
        showStatistics = true;
    }
}
