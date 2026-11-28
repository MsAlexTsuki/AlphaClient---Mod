package com.alpharip.staffmod.predictions.armor;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

/** Reads the local player's equipped armor and finds pieces below the configured threshold. */
public final class ArmorDurabilityWarningScanner {
    public ArmorDurabilityWarningResult scan(ItemStack[] armorInventory,
                                             ArmorDurabilityWarningConfig config) {
        if (config == null) throw new IllegalArgumentException("config");

        List<ArmorDurabilityWarningPiece> lowPieces =
                new ArrayList<ArmorDurabilityWarningPiece>();
        if (armorInventory == null) {
            return new ArmorDurabilityWarningResult(lowPieces);
        }

        for (int index = 0; index < armorInventory.length && index < 4; index++) {
            ItemStack stack = armorInventory[index];
            if (stack == null || !stack.isItemStackDamageable()) continue;

            int maximum = stack.getMaxDamage();
            if (maximum <= 0) continue;

            int remaining = Math.max(0, maximum - stack.getItemDamage());
            double remainingPercent = (remaining * 100.0D) / maximum;
            if (remainingPercent <= config.getWarningThresholdPercent()) {
                lowPieces.add(new ArmorDurabilityWarningPiece(
                        slotForInventoryIndex(index),
                        stack.getDisplayName(),
                        remaining,
                        maximum,
                        remainingPercent));
            }
        }

        return new ArmorDurabilityWarningResult(lowPieces);
    }

    private ArmorSlot slotForInventoryIndex(int index) {
        // Minecraft 1.8.9 armorInventory order: boots, leggings, chestplate, helmet.
        switch (index) {
            case 0: return ArmorSlot.BOOTS;
            case 1: return ArmorSlot.LEGGINGS;
            case 2: return ArmorSlot.CHESTPLATE;
            case 3: return ArmorSlot.HELMET;
            default: throw new IllegalArgumentException("Unsupported armor inventory index: " + index);
        }
    }
}
