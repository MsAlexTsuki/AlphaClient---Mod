package com.alpharip.staffmod.ChestItemTotal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads the lower inventory of the chest GUI and aggregates stacks by registry ID. */
final class ChestItemTotalScanner {
    private ChestItemTotalSnapshot snapshot = ChestItemTotalSnapshot.empty();
    private GuiContainer lastScreen;

    void refresh() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !(mc.currentScreen instanceof GuiChest)) {
            lastScreen = null;
            snapshot = ChestItemTotalSnapshot.empty();
            return;
        }

        GuiContainer screen = (GuiContainer) mc.currentScreen;
        if (!(screen.inventorySlots instanceof ContainerChest)) {
            lastScreen = screen;
            snapshot = ChestItemTotalSnapshot.empty();
            return;
        }

        lastScreen = screen;
        IInventory chest = ((ContainerChest) screen.inventorySlots).getLowerChestInventory();
        if (chest == null) {
            snapshot = ChestItemTotalSnapshot.empty();
            return;
        }

        Map<String, MutableTotal> totals = new LinkedHashMap<String, MutableTotal>();
        int totalItems = 0;

        for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
            ItemStack stack = chest.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0 || stack.getItem() == null) continue;

            String itemId = String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem()));
            MutableTotal current = totals.get(itemId);
            if (current == null) {
                current = new MutableTotal(stack.getDisplayName());
                totals.put(itemId, current);
            }
            current.count += stack.stackSize;
            totalItems += stack.stackSize;
        }

        List<ChestItemTotalEntry> entries = new ArrayList<ChestItemTotalEntry>();
        for (Map.Entry<String, MutableTotal> entry : totals.entrySet()) {
            entries.add(new ChestItemTotalEntry(entry.getKey(),
                    entry.getValue().displayName, entry.getValue().count));
        }
        snapshot = new ChestItemTotalSnapshot(entries, totalItems);
    }

    ChestItemTotalSnapshot getSnapshot() {
        return snapshot;
    }

    private static final class MutableTotal {
        private final String displayName;
        private int count;

        private MutableTotal(String displayName) {
            this.displayName = displayName;
        }
    }
}
