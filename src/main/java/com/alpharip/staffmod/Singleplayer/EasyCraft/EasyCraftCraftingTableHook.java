package com.alpharip.staffmod.Singleplayer.EasyCraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/**
 * Narrow hook boundary for the vanilla Crafting Table screen only.
 *
 * The existing GuiCrafting screen must call createButton() while building its
 * button list and open() when that button is clicked. No player-inventory
 * screen hook is provided or intended.
 */
public final class EasyCraftCraftingTableHook {
    public static final int BUTTON_ID = 0xEC01;

    private EasyCraftCraftingTableHook() { }

    public static EasyCraftTableButton createButton(int guiLeft, int guiTop) {
        return new EasyCraftTableButton(BUTTON_ID, guiLeft + 8, guiTop + 5);
    }

    public static boolean handleButton(GuiScreen parent, int buttonId,
                                       EasyCraftCraftingAdapter adapter,
                                       EasyCraftRecipeCatalog catalog,
                                       EasyCraftFavorites favorites) {
        if (buttonId != BUTTON_ID) return false;
        if (parent == null || adapter == null || !adapter.isCraftingTableOpen()) return true;
        Minecraft.getMinecraft().displayGuiScreen(new EasyCraftGui(parent, adapter, catalog, favorites));
        return true;
    }
}
