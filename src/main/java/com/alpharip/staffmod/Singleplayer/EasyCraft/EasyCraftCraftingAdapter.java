package com.alpharip.staffmod.Singleplayer.EasyCraft;

/**
 * Integration boundary for the open vanilla Crafting Table.
 * A production adapter must verify the active GuiCrafting/ContainerWorkbench,
 * check the player's actual inventory, and move items through normal container
 * interactions. This prototype deliberately does not bypass Minecraft inventory rules.
 */
public interface EasyCraftCraftingAdapter {
    boolean isCraftingTableOpen();

    /** Returns the number of complete crafts supported by the player's current inventory. */
    int getAvailableCraftCount(EasyCraftRecipe recipe);

    /** Requests the given number of crafts through the active vanilla crafting-table container. */
    boolean craft(EasyCraftRecipe recipe, int amount);
}
