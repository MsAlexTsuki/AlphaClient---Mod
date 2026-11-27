package com.alpharip.staffmod.store;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiInventory;

/**
 * Renders the currently logged-in Minecraft player with the skin already
 * resolved by Minecraft's authenticated profile/session pipeline.
 */
public final class StoreSkinPreview {
    private StoreSkinPreview() { }

    public static void draw(int x, int y, int scale, EntityPlayerSP player) {
        if (player == null) return;
        GuiInventory.drawEntityOnScreen(x, y, scale, 0.0F, 0.0F, player);
    }
}
