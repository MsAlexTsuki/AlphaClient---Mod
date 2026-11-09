package com.alpharip.staffmod.singleplayer.privatevault;

import net.minecraft.inventory.InventoryBasic;

/** The 54-slot inventory used by one numbered private vault. */
public final class AlphaPrivateVaultInventory extends InventoryBasic {
    private final int vaultNumber;
    private volatile boolean persistenceEnabled;

    public AlphaPrivateVaultInventory(int vaultNumber) {
        super("Alpha Private Vault #" + vaultNumber, false, 54);
        this.vaultNumber = vaultNumber;
    }

    public int getVaultNumber() {
        return vaultNumber;
    }

    /** Enables autosaving only after the existing data has finished loading. */
    public void enablePersistence() {
        this.persistenceEnabled = true;
    }

    /**
     * ContainerChest calls markDirty after inventory mutations on the integrated
     * server. Queue a debounced save instead of serializing and writing the vault
     * for every individual slot mutation.
     */
    @Override
    public void markDirty() {
        super.markDirty();
        if (persistenceEnabled) {
            AlphaPrivateVaultManager.getInstance().queueVaultSave(this);
        }
    }
}
