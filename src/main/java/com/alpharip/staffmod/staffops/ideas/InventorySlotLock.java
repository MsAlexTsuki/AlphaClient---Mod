package com.alpharip.staffmod.staffops.ideas;

/**
 * Foundation for InventorySlotLock; configuration/state only.
 * Marks selected inventory slots as protected from accidental movement. Runtime slot handling is not implemented.
 */
public class InventorySlotLock {
    public boolean enabled = false;
    public String category = "Inventory/utility";
    public String style = "default";
}
