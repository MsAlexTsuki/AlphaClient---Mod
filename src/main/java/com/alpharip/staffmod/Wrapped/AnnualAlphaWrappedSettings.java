package com.alpharip.staffmod.wrapped;

/** Local-only display and opt-in settings; no remote account setting is written. */
public final class AnnualAlphaWrappedSettings {
    private boolean enabled;
    private int selectedYear;

    public AnnualAlphaWrappedSettings(int selectedYear) {
        this.enabled = false;
        this.selectedYear = selectedYear;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getSelectedYear() { return selectedYear; }
    public void setSelectedYear(int selectedYear) { this.selectedYear = selectedYear; }
}
