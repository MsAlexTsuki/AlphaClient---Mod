// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.winlosetracker;

/** Display preferences persisted with the local tracker data. */
public final class WinLossTrackerSettings {
    private boolean showOverallStatistics = true;
    private boolean showCurrentAddressStatistics = true;
    private boolean showIndividualAddressStatistics;

    public boolean isShowOverallStatistics() { return showOverallStatistics; }
    public void setShowOverallStatistics(boolean enabled) { showOverallStatistics = enabled; }

    public boolean isShowCurrentAddressStatistics() { return showCurrentAddressStatistics; }
    public void setShowCurrentAddressStatistics(boolean enabled) { showCurrentAddressStatistics = enabled; }

    public boolean isShowIndividualAddressStatistics() { return showIndividualAddressStatistics; }
    public void setShowIndividualAddressStatistics(boolean enabled) { showIndividualAddressStatistics = enabled; }
}
