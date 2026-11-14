package com.alpharip.staffmod.store.addons;

import com.alpharip.staffmod.store.Store;
import com.alpharip.staffmod.store.StoreGui;
import net.minecraft.client.gui.GuiScreen;

/** Backwards-compatible entry point for opening the Addons page in the central Store UI. */
public final class AddonStoreGui extends StoreGui {
    public AddonStoreGui(GuiScreen parent, AddonStore addonStore) {
        super(parent, new Store(addonStore));
    }
}
