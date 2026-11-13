package com.alpharip.staffmod.ArrowCounter;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;

/** Reads and totals arrow stacks in the player's inventory. */
public final class ArrowCounterScanner {
    private int arrowCount;

    public void refresh() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.thePlayer.inventory == null) {
            arrowCount = 0;
            return;
        }

        int total = 0;
        ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;
        if (inventory != null) {
            for (ItemStack stack : inventory) {
                if (stack != null && stack.getItem() instanceof ItemArrow && stack.stackSize > 0) {
                    total += stack.stackSize;
                }
            }
        }
        arrowCount = total;
    }

    public int getArrowCount() {
        return arrowCount;
    }
}
