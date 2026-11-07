package com.alpharip.staffmod.singleplayer.privatevault;

import net.minecraft.client.Minecraft;
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
     * server. Save on the client thread because the existing world-local storage
     * manager resolves the active Minecraft save from the client instance.
     */
    @Override
    public void markDirty() {
        super.markDirty();
        if (!persistenceEnabled) {
            return;
        }

        final int number = vaultNumber;
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                AlphaPrivateVaultManager.getInstance().saveVault(number,
                        AlphaPrivateVaultInventory.this);
            }
        });
    }
}
