package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** Applies the agreed 90/10 vanilla/custom roll split to the normal table result. */
public final class CustomEnchantmentTableHooks {
    private CustomEnchantmentTableHooks() { }

    public static List<EnchantmentData> replaceOne(List<EnchantmentData> vanilla,
                                                    Object containerObject, ItemStack tableStack,
                                                    int enchantSlot, int enchantLevel) {
        if (vanilla == null || !(containerObject instanceof ContainerEnchantment)
                || !CustomEnchantmentAccess.isAvailable() || tableStack == null
                || !CustomEnchantments.isRegistered()) return vanilla;
        ContainerEnchantment container = (ContainerEnchantment) containerObject;
        long seed = 31L * container.xpSeed + 17L * tableStack.hashCode()
                + 131L * enchantSlot + 977L * enchantLevel;
        java.util.Random random = new java.util.Random(seed);
        if (random.nextInt(100) >= 10) return vanilla;

        List<CustomEnchantment> eligible = new ArrayList<CustomEnchantment>();
        for (CustomEnchantment custom : CustomEnchantments.getAll()) {
            if (custom.canApply(tableStack) && !conflicts(vanilla, custom)) eligible.add(custom);
        }
        if (eligible.isEmpty()) return vanilla;

        List<CustomEnchantment.Rarity> availableRarities = new ArrayList<CustomEnchantment.Rarity>();
        int totalRarityWeight = 0;
        for (CustomEnchantment.Rarity rarity : CustomEnchantment.Rarity.values()) {
            boolean available = false;
            for (CustomEnchantment enchantment : eligible) {
                if (enchantment.getRarity() == rarity) { available = true; break; }
            }
            if (available) {
                availableRarities.add(rarity);
                totalRarityWeight += rarity.weight;
            }
        }
        int rarityRoll = random.nextInt(totalRarityWeight);
        CustomEnchantment.Rarity selectedRarity = availableRarities.get(0);
        for (CustomEnchantment.Rarity rarity : availableRarities) {
            if (rarityRoll < rarity.weight) { selectedRarity = rarity; break; }
            rarityRoll -= rarity.weight;
        }
        List<CustomEnchantment> candidates = new ArrayList<CustomEnchantment>();
        for (CustomEnchantment enchantment : eligible)
            if (enchantment.getRarity() == selectedRarity) candidates.add(enchantment);

        CustomEnchantment selected = candidates.get(random.nextInt(candidates.size()));
        int level = 1 + random.nextInt(selected.getMaxLevel());
        List<EnchantmentData> result = new ArrayList<EnchantmentData>(vanilla);
        if (result.isEmpty()) result.add(new EnchantmentData(selected, level));
        else result.set(random.nextInt(result.size()), new EnchantmentData(selected, level));
        return result;
    }

    private static boolean conflicts(List<EnchantmentData> existing, Enchantment candidate) {
        if (existing == null) return false;
        for (EnchantmentData data : existing) {
            if (data != null && data.enchantmentobj != null
                    && (!data.enchantmentobj.canApplyTogether(candidate)
                    || !candidate.canApplyTogether(data.enchantmentobj))) return true;
        }
        return false;
    }
}