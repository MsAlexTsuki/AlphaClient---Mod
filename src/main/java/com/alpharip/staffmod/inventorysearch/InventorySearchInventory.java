package com.alpharip.staffmod.inventorysearch;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ContainerPlayer;

/** Matches the player's inventory screen. */
public final class InventorySearchInventory {
    private InventorySearchInventory() {
    }

    public static boolean supports(GuiContainer gui) {
        return gui instanceof GuiInventory || gui.inventorySlots instanceof ContainerPlayer;
    }
}
