package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Applies the agreed 90/10 vanilla/custom roll split to the normal table result. */
public final class CustomEnchantmentTableHooks {
    private static final Random RANDOM = new Random();
    private CustomEnchantmentTableHooks() { }

    public static List<EnchantmentData> replaceOne(List<EnchantmentData> vanilla,
                                                    Object containerObject, ItemStack tableStack) {
        if (vanilla == null || !(containerObject instanceof ContainerEnchantment)
                || !CustomEnchantmentAccess.isAvailable() || tableStack == null
                || !CustomEnchantments.isRegistered()) return vanilla;
        if (RANDOM.nextInt(100) >= 10) return vanilla;

        List<CustomEnchantment> eligible = new ArrayList<CustomEnchantment>();
        for (Enchantment enchantment : Enchantment.REGISTRY) {
            if (!(enchantment instanceof CustomEnchantment)) continue;
            CustomEnchantment custom = (CustomEnchantment) enchantment;
            if (custom.canApply(tableStack) && !conflicts(vanilla, custom)) eligible.add(custom);
        }
        if (eligible.isEmpty()) return vanilla;

        int roll = RANDOM.nextInt(100);
        CustomEnchantment.Rarity rarity = roll < 55 ? CustomEnchantment.Rarity.UNCOMMON
                : roll < 85 ? CustomEnchantment.Rarity.RARE : CustomEnchantment.Rarity.LEGENDARY;
        List<CustomEnchantment> candidates = new ArrayList<CustomEnchantment>();
        for (CustomEnchantment enchantment : eligible)
            if (enchantment.getRarity() == rarity) candidates.add(enchantment);
        if (candidates.isEmpty()) candidates = eligible;

        CustomEnchantment selected = candidates.get(RANDOM.nextInt(candidates.size()));
        int level = 1 + RANDOM.nextInt(selected.getMaxLevel());
        List<EnchantmentData> result = new ArrayList<EnchantmentData>(vanilla);
        if (result.isEmpty()) result.add(new EnchantmentData(selected, level));
        else result.set(RANDOM.nextInt(result.size()), new EnchantmentData(selected, level));
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