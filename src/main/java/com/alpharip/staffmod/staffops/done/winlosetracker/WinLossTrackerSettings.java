package com.alpharip.staffmod.winlosetracker;

/** User-configurable visibility options for Win/Loss Tracker statistics. */
public final class WinLossTrackerSettings {
    private boolean showOverallStatistics = true;
    private boolean showCurrentAddressStatistics = true;
    private boolean showIndividualAddressStatistics = false;

    public boolean isShowOverallStatistics() { return showOverallStatistics; }
    public void setShowOverallStatistics(boolean value) { showOverallStatistics = value; }

    public boolean isShowCurrentAddressStatistics() { return showCurrentAddressStatistics; }
    public void setShowCurrentAddressStatistics(boolean value) { showCurrentAddressStatistics = value; }

    public boolean isShowIndividualAddressStatistics() { return showIndividualAddressStatistics; }
    public void setShowIndividualAddressStatistics(boolean value) { showIndividualAddressStatistics = value; }
}
