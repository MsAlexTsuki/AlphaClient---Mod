package com.alpharip.staffmod.InventorySearch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryEnderChest;
/** Matches the player's ender chest. */
public final class InventorySearchEnderChest {
 private InventorySearchEnderChest(){}
 public static boolean supports(GuiChest gui){return gui.inventorySlots instanceof ContainerChest&&((ContainerChest)gui.inventorySlots).getLowerChestInventory() instanceof InventoryEnderChest;}
}