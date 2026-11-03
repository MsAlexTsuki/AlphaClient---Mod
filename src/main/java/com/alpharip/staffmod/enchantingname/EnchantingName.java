package com.alpharip.staffmod.enchantingname;

/**
 * Foundation for displaying the actual enchantment name directly in the
 * Minecraft 1.8.9 enchanting table.
 *
 * <p>This class only stores the feature state and validates a name that has
 * already been resolved by an authoritative enchantment preview. It does not
 * predict the enchanting table result or hook into the GUI.</p>
 *
 * <p>Runtime integration is intentionally not implemented in the mod-ideas
 * branch.</p>
 */
public class EnchantingName {

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether a resolved enchantment name should be displayed.
     *
     * @param resolvedEnchantmentName the actual enchantment name, already
     *                                resolved by the enchanting logic
     * @return true when the feature is enabled and the name is not empty
     */
    public boolean shouldDisplayName(String resolvedEnchantmentName) {
        return enabled
                && resolvedEnchantmentName != null
                && !resolvedEnchantmentName.trim().isEmpty();
    }
}
