package com.alpharip.staffmod.InventorySearch;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryEnderChest;
/** Matches normal chest containers and excludes the ender chest. */
public final class InventorySearchChest {
 private InventorySearchChest(){}
 public static boolean supports(GuiChest gui){return gui.inventorySlots instanceof ContainerChest&&!(((ContainerChest)gui.inventorySlots).getLowerChestInventory() instanceof InventoryEnderChest);}
}