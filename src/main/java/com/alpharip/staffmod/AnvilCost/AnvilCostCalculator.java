package com.alpharip.staffmod.AnvilCost;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.Collections;
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
    private AnvilCostCalculator() {
    }

    public static List<Preview> calculate(ItemStack target, EntityPlayer player) {
        if (target == null || player == null || player.inventory == null || player.worldObj == null) {
            return Collections.emptyList();
        }

        List<Preview> previews = new ArrayList<Preview>();
        Set<String> seenInputs = new HashSet<String>();
        ItemStack[] inventory = player.inventory.mainInventory;

        if (inventory == null) {
            return previews;
        }

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
                        describeOperation(target, candidate),
                        candidate.getDisplayName(),
                        levels,
                        materials,
                        candidate.getItem() == Items.enchanted_book
                ));
            } catch (RuntimeException exception) {
                // An unusual modded item must not break the item's normal tooltip.
            }
        }

        return previews;
    }

    private static String createInputKey(ItemStack stack) {
        String tag = stack.getTagCompound() == null ? "" : stack.getTagCompound().toString();
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getMetadata() + ":"
                + stack.getItemDamage() + ":" + stack.getRepairCost() + ":"
                + stack.stackSize + ":" + tag;
    }

    private static String describeOperation(ItemStack target, ItemStack candidate) {
        if (candidate.getItem() == Items.enchanted_book) {
            String enchantments = describeEnchantments(candidate);
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

    private static String describeEnchantments(ItemStack book) {
        Map<Integer, Integer> enchantments = new TreeMap<Integer, Integer>(
                EnchantmentHelper.getEnchantments(book)
        );
        StringBuilder result = new StringBuilder();

        for (Map.Entry<Integer, Integer> entry : enchantments.entrySet()) {
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
        private final boolean enchantedBook;

        private Preview(String operation, String materialName, int levels,
                        int materialCount, boolean enchantedBook) {
            this.operation = operation;
            this.materialName = materialName;
            this.levels = levels;
            this.materialCount = materialCount;
            this.enchantedBook = enchantedBook;
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

        public boolean isEnchantedBook() {
            return enchantedBook;
        }
    }
}
