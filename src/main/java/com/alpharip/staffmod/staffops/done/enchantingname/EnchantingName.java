package com.alpharip.staffmod.enchantingname;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.ContainerEnchantment;

/**
 * Resolves the normal Minecraft enchantment text for each enchanting-table
 * option, replacing the Standard Galactic Alphabet text shown by vanilla.
 *
 * <p>Minecraft 1.8.9 synchronizes one enchantment clue per option through
 * ContainerEnchantment.enchantmentIds. This class reads that clue and resolves
 * its localized enchantment name and level, for example "Sharpness IV".</p>
 *
 * <p>The future GUI integration should draw this returned text where vanilla
 * draws the obfuscated glyph text. No hover interaction is needed. This only
 * replaces the displayed clue; it does not attempt to list every enchantment
 * that may be applied by the selected option.</p>
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
     * Gets the normal-language text for one enchanting-table option.
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
