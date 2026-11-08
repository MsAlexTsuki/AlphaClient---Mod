// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.inventorysorter;

import net.minecraft.client.Minecraft;

/** Availability checks shared by the Singleplayer menu and Inventory Sorter GUI. */
public final class InventorySorterAccess {
    private InventorySorterAccess() {
    }

    public static boolean isAvailable(Minecraft minecraft) {
        return minecraft != null
                && minecraft.isSingleplayer()
                && minecraft.thePlayer != null
                && minecraft.theWorld != null
                && minecraft.getIntegratedServer() != null;
    }
}
