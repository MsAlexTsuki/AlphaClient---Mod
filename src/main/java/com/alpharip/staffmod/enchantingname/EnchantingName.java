package com.alpharip.staffmod.enchantingname;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.ContainerEnchantment;

/**
 * Idea foundation for displaying the actual enchantment clue directly beside
 * each option in the Minecraft 1.8.9 enchanting table.
 *
 * <p>Minecraft 1.8.9 already calculates one real enchantment clue per option
 * on the server and synchronizes it to the client through
 * ContainerEnchantment.enchantmentIds. Each value packs the enchantment ID
 * into the low 8 bits and the enchantment level into the next 8 bits.</p>
 *
 * <p>This class reads that synchronized clue and resolves its localized name
 * through the registered Enchantment. It does not reproduce the random
 * enchantment algorithm, infer results from xpSeed, or claim to reveal every
 * enchantment that will be applied. The clue is the same single enchantment
 * vanilla displays when the player hovers over an option.</p>
 *
 * <p>Idea-only code in the mod-ideas branch. No GUI hook or runtime
 * registration is included.</p>
 */
public class EnchantingName {

    private static final int OPTION_COUNT = 3;
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the authoritative enchantment clue for one option.
     *
     * @param container the active client-side enchanting container
     * @param optionIndex option index from 0 to 2
     * @return the localized enchantment name and level, or null when no clue
     *         is available for this option
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
