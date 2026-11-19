package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Applies the agreed 90/10 vanilla/custom roll split to the normal table result. */
public final class CustomEnchantmentTableHooks {
    private CustomEnchantmentTableHooks() { }

    public static List<EnchantmentData> replaceOne(List<EnchantmentData> vanilla,
                                                    Object containerObject, Object tableStackObject,
                                                    int enchantSlot, int enchantLevel) {
        if (vanilla == null || !(containerObject instanceof ContainerEnchantment)
                || !CustomEnchantmentAccess.isAvailable() || !(tableStackObject instanceof ItemStack)
                || !CustomEnchantments.isRegistered()) return vanilla;
        ItemStack tableStack = (ItemStack) tableStackObject;
        ContainerEnchantment container = (ContainerEnchantment) containerObject;
        long seed = 31L * container.xpSeed + 17L * tableStack.hashCode()
                + 131L * enchantSlot + 977L * enchantLevel;
        java.util.Random random = new java.util.Random(seed);
        if (random.nextInt(100) >= 10) {
            upgradeVanillaLevel(vanilla, random, enchantLevel);
            return vanilla;
        }

        List<CustomEnchantment> eligible = new ArrayList<CustomEnchantment>();
        for (CustomEnchantment custom : CustomEnchantments.getAll()) {
            if (custom.canApply(tableStack) && !conflicts(vanilla, custom)
                    && !conflictsWithItem(tableStack, custom)) eligible.add(custom);
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
        List<EnchantmentData> result = new ArrayList<EnchantmentData>();
        result.add(new EnchantmentData(selected, level));
        return result;
    }


    private static void upgradeVanillaLevel(List<EnchantmentData> results,
                                            java.util.Random random, int tablePower) {
        if (results == null || results.isEmpty() || tablePower < 20
                || random.nextInt(100) >= 25) return;
        List<Integer> eligible = new ArrayList<Integer>();
        for (int i = 0; i < results.size(); i++) {
            Enchantment enchantment = results.get(i).enchantmentobj;
            int vanillaMax = vanillaMaximum(enchantment);
            if (vanillaMax > 0 && enchantment.getMaxLevel() > vanillaMax) eligible.add(i);
        }
        if (eligible.isEmpty()) return;
        int index = eligible.get(random.nextInt(eligible.size()));
        EnchantmentData current = results.get(index);
        int vanillaMax = vanillaMaximum(current.enchantmentobj);
        int extendedMax = current.enchantmentobj.getMaxLevel();
        int levelRange = extendedMax - vanillaMax;
        int extraLevels = Math.min(levelRange, 1 + ((tablePower - 20) * levelRange) / 10);
        if (extraLevels <= 0) return;
        int level = vanillaMax + 1 + random.nextInt(extraLevels);
        results.set(index, new EnchantmentData(current.enchantmentobj, level));
    }

    private static int vanillaMaximum(Enchantment enchantment) {
        if (enchantment == Enchantment.sharpness || enchantment == Enchantment.smite
                || enchantment == Enchantment.baneOfArthropods || enchantment == Enchantment.efficiency
                || enchantment == Enchantment.power) return 5;
        if (enchantment == Enchantment.protection || enchantment == Enchantment.fireProtection
                || enchantment == Enchantment.blastProtection || enchantment == Enchantment.projectileProtection
                || enchantment == Enchantment.featherFalling) return 4;
        if (enchantment == Enchantment.unbreaking || enchantment == Enchantment.fortune
                || enchantment == Enchantment.looting || enchantment == Enchantment.respiration
                || enchantment == Enchantment.depthStrider) return 3;
        if (enchantment == Enchantment.punch || enchantment == Enchantment.fireAspect
                || enchantment == Enchantment.knockback) return 2;
        if (enchantment == Enchantment.flame || enchantment == Enchantment.infinity
                || enchantment == Enchantment.aquaAffinity || enchantment == Enchantment.silkTouch) return 1;
        return 0;
    }


    private static boolean conflictsWithItem(ItemStack stack, CustomEnchantment candidate) {
        Map<Enchantment, Integer> existing = net.minecraft.enchantment.EnchantmentHelper.getEnchantments(stack);
        if (existing == null) return false;
        for (Enchantment enchantment : existing.keySet()) {
            if (enchantment != null && (!enchantment.canApplyTogether(candidate)
                    || !candidate.canApplyTogether(enchantment))) return true;
        }
        return false;
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