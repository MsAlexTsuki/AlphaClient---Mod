package com.alpharip.staffmod.singleplayer.privatevault;

import net.minecraft.inventory.InventoryBasic;

/** The 54-slot chest inventory used by one numbered private vault. */
public final class AlphaPrivateVaultInventory extends InventoryBasic {
    private final int vaultNumber;

    public AlphaPrivateVaultInventory(int vaultNumber) {
        super("Alpha Private Vault #" + vaultNumber, false, 54);
        this.vaultNumber = vaultNumber;
    }

    public int getVaultNumber() {
        return vaultNumber;
    }
}
