package com.alpharip.staffmod.enchantingname;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Idea foundation for showing enchantment names directly in the Minecraft
 * 1.8.9 enchanting table, without requiring the player to hover over an option.
 *
 * <p>This class stores names that have already been resolved for each of the
 * three enchanting-table options. It deliberately does not calculate or guess
 * enchantment results. A future runtime implementation must obtain the actual
 * result from an authoritative preview source before passing names to this
 * class.</p>
 *
 * <p>This is idea-only code in the mod-ideas branch. It is not registered,
 * connected to the enchanting GUI, or active in AlphaClient.</p>
 */
public class EnchantingName {

    private static final int OPTION_COUNT = 3;

    private boolean enabled = true;

    /*
     * The key is the enchanting-table option index (0, 1, or 2).
     * A list is used because one option can result in more than one enchantment.
     */
    private final Map<Integer, List<String>> optionNames = new HashMap<Integer, List<String>>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Stores the resolved enchantment names for one enchanting-table option.
     *
     * @param optionIndex option index from 0 to 2
     * @param resolvedNames actual, already-resolved enchantment display names
     */
    public void setOptionNames(int optionIndex, List<String> resolvedNames) {
        validateOptionIndex(optionIndex);

        if (resolvedNames == null || resolvedNames.isEmpty()) {
            optionNames.remove(optionIndex);
            return;
        }

        List<String> cleanNames = new ArrayList<String>();
        for (String name : resolvedNames) {
            if (name != null && !name.trim().isEmpty()) {
                cleanNames.add(name.trim());
            }
        }

        if (cleanNames.isEmpty()) {
            optionNames.remove(optionIndex);
        } else {
            optionNames.put(optionIndex, cleanNames);
        }
    }

    /**
     * Returns the resolved names for an option.
     *
     * @param optionIndex option index from 0 to 2
     * @return an immutable list; empty when no names have been resolved
     */
    public List<String> getOptionNames(int optionIndex) {
        validateOptionIndex(optionIndex);

        List<String> names = optionNames.get(optionIndex);
        if (names == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(names);
    }

    /**
     * Indicates whether a name should currently be shown for an option.
     */
    public boolean shouldDisplayOption(int optionIndex) {
        return enabled && !getOptionNames(optionIndex).isEmpty();
    }

    /**
     * Clears names when the item, enchantment seed, or available options change.
     */
    public void clearOptions() {
        optionNames.clear();
    }

    private void validateOptionIndex(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= OPTION_COUNT) {
            throw new IllegalArgumentException("Enchanting option index must be between 0 and 2.");
        }
    }
}
