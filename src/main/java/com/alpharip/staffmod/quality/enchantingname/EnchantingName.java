// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.quality.enchantingname;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.ContainerEnchantment;

/**
 * Resolves the localized enchantment clue for each of the three
 * Minecraft 1.8.9 enchanting-table options.
 *
 * The container synchronizes the enchantment clue for each option. The
 * encoded value stores the enchantment ID in the low byte and its level
 * in the next byte.
 */
public final class EnchantingName {
    private static final int OPTION_COUNT = 3;

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the localized enchantment name and level for an option.
     *
     * @param container active client-side enchanting container
     * @param optionIndex option index from 0 through 2
     * @return localized name, or null if no valid clue is available
     */
    public String getOptionDisplayName(ContainerEnchantment container, int optionIndex) {
        validateOptionIndex(optionIndex);

        if (!enabled || container == null
                || container.enchantmentIds == null
                || container.enchantmentIds.length <= optionIndex) {
            return null;
        }

        int encodedClue = container.enchantmentIds[optionIndex];
        if (encodedClue < 0) {
            return null;
        }

        int enchantmentId = encodedClue & 255;
        int enchantmentLevel = (encodedClue >> 8) & 255;
        if (enchantmentLevel <= 0) {
            return null;
        }

        Enchantment enchantment = Enchantment.getEnchantmentById(enchantmentId);
        if (enchantment == null) {
            return null;
        }

        return enchantment.getTranslatedName(enchantmentLevel);
    }

    public boolean shouldDisplayOption(ContainerEnchantment container, int optionIndex) {
        return getOptionDisplayName(container, optionIndex) != null;
    }

    private void validateOptionIndex(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= OPTION_COUNT) {
            throw new IllegalArgumentException(
                    "Enchanting option index must be between 0 and 2.");
        }
    }
}
