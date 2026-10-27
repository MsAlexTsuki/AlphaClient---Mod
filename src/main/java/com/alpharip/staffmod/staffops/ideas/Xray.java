package com.alpharip.staffmod.staffops.ideas;

/**
 * Idea foundation for the compact Xray StaffMod.
 * Planned controls: master toggle plus separate ores, chests, and spawners toggles.
 * This class does not implement Minecraft world rendering or block visibility.
 */
public class Xray {
    private boolean enabled;
    private boolean showOres = true;
    private boolean showChests = true;
    private boolean showSpawners = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isShowOres() {
        return showOres;
    }

    public void setShowOres(boolean showOres) {
        this.showOres = showOres;
    }

    public boolean isShowChests() {
        return showChests;
    }

    public void setShowChests(boolean showChests) {
        this.showChests = showChests;
    }

    public boolean isShowSpawners() {
        return showSpawners;
    }

    public void setShowSpawners(boolean showSpawners) {
        this.showSpawners = showSpawners;
    }
}
