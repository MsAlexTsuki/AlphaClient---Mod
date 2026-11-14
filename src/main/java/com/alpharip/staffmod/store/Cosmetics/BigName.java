package com.alpharip.staffmod.staffops.ideas;

/**
 * Idea foundation for the Big Name StaffMod.
 * The intended feature is to render player nametags larger for improved readability.
 * Runtime rendering and module integration are not implemented here.
 */
public class BigName {
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
