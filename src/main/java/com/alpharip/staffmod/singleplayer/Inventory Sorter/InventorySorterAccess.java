package com.alpharip.staffmod.singleplayer.inventorysorter;

import net.minecraft.client.Minecraft;

/** Restricts Inventory Sorter to an active integrated singleplayer world. */
public final class InventorySorterAccess {
    private InventorySorterAccess() {
    }

    public static boolean isAvailable(Minecraft mc) {
        return mc != null
                && mc.isSingleplayer()
                && mc.getIntegratedServer() != null
                && mc.thePlayer != null
                && mc.theWorld != null;
    }
}
