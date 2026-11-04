package com.alpharip.staffmod.staffops.done.AnvilCost;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Calculates inventory anvil previews by running Minecraft 1.8.9's own
 * ContainerRepair logic against copied stacks. The player's inventory is never
 * used as an input or modified by the simulation.
 */
public final class AnvilCostCalculator {
    private static int cachedTick = -1;
    private static final Map<String, List<Preview>> CACHE = new HashMap<String, List<Preview>>();

    private AnvilCostCalculator() {
    }

    public static List<Preview> calculate(ItemStack target, EntityPlayer player) {
        if (target == null || player == null || player.inventory == null || player.worldObj == null) {
            return Collections.emptyList();
        }

        ItemStack[] inventory = player.inventory.mainInventory;
        if (inventory == null) {
            return Collections.emptyList();
        }

        String cacheKey = createInputKey(target) + "|" + createInventoryKey(inventory);
        synchronized (AnvilCostCalculator.class) {
            if (cachedTick != player.ticksExisted) {
                CACHE.clear();
                cachedTick = player.ticksExisted;
            }
            List<Preview> cached = CACHE.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        List<Preview> previews = new ArrayList<Preview>();
        Set<String> seenInputs = new HashSet<String>();

        for (ItemStack candidate : inventory) {
            if (candidate == null || candidate.stackSize <= 0 || candidate == target) {
                continue;
            }

            String inputKey = createInputKey(candidate);
            if (!seenInputs.add(inputKey)) {
                continue;
            }

            try {
                ContainerRepair simulation = new ContainerRepair(player.inventory, player.worldObj, player);
                simulation.getSlot(0).putStack(target.copy());
                simulation.getSlot(1).putStack(candidate.copy());
                simulation.updateRepairOutput();

                ItemStack result = simulation.getSlot(2).getStack();
                int levels = simulation.maximumCost;

                if (result == null || levels <= 0) {
                    continue;
                }

                int materials = simulation.materialCost;
                if (materials <= 0) {
                    materials = 1;
                }

                previews.add(new Preview(
                        describeOperation(target, candidate, result),
                        candidate.getDisplayName(),
                        levels,
                        materials
                ));
            } catch (RuntimeException exception) {
                // An unusual modded item must not break the item's normal tooltip.
            }
        }

        Collections.sort(previews, new Comparator<Preview>() {
            @Override
            public int compare(Preview first, Preview second) {
                return Integer.compare(first.getLevels(), second.getLevels());
            }
        });

        List<Preview> result = Collections.unmodifiableList(new ArrayList<Preview>(previews));
        synchronized (AnvilCostCalculator.class) {
            CACHE.put(cacheKey, result);
        }
        return result;
    }

    private static String createInventoryKey(ItemStack[] inventory) {
        StringBuilder key = new StringBuilder();
        for (ItemStack stack : inventory) {
            key.append(stack == null ? "empty" : createInputKey(stack)).append('|');
        }
        return key.toString();
    }

    private static String createInputKey(ItemStack stack) {
        String tag = stack.getTagCompound() == null ? "" : stack.getTagCompound().toString();
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getMetadata() + ":"
                + stack.getItemDamage() + ":" + stack.getRepairCost() + ":"
                + stack.stackSize + ":" + tag;
    }

    private static String describeOperation(ItemStack target, ItemStack candidate, ItemStack result) {
        if (candidate.getItem() == Items.enchanted_book) {
            String enchantments = describeAppliedEnchantments(target, result);
            return enchantments.isEmpty() ? "Enchanted Book" : enchantments + " Book";
        }

        if (target.getItem().getIsRepairable(target, candidate)) {
            return "Repair with " + candidate.getDisplayName();
        }

        if (target.getItem() == candidate.getItem() && target.isItemStackDamageable()) {
            return "Combine with " + candidate.getDisplayName();
        }

        return "Use " + candidate.getDisplayName();
    }

    private static String describeAppliedEnchantments(ItemStack original, ItemStack resultStack) {
        Map<Integer, Integer> originalEnchantments = EnchantmentHelper.getEnchantments(original);
        Map<Integer, Integer> resultEnchantments = new TreeMap<Integer, Integer>(
                EnchantmentHelper.getEnchantments(resultStack)
        );
        StringBuilder result = new StringBuilder();

        for (Map.Entry<Integer, Integer> entry : resultEnchantments.entrySet()) {
            int previousLevel = originalEnchantments.containsKey(entry.getKey())
                    ? originalEnchantments.get(entry.getKey()) : 0;
            if (entry.getValue() <= previousLevel) {
                continue;
            }
            Enchantment enchantment = Enchantment.getEnchantmentById(entry.getKey());
            if (enchantment == null) {
                continue;
            }

            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(enchantment.getTranslatedName(entry.getValue()));
        }

        return result.toString();
    }

    public static final class Preview {
        private final String operation;
        private final String materialName;
        private final int levels;
        private final int materialCount;

        private Preview(String operation, String materialName, int levels,
                        int materialCount) {
            this.operation = operation;
            this.materialName = materialName;
            this.levels = levels;
            this.materialCount = materialCount;
        }

        public String getOperation() {
            return operation;
        }

        public String getMaterialName() {
            return materialName;
        }

        public int getLevels() {
            return levels;
        }

        public int getMaterialCount() {
            return materialCount;
        }
    }
}
