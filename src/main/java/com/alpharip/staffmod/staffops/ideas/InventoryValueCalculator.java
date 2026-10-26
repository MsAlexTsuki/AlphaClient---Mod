package com.alpharip.staffmod.staffops.ideas;

/** Foundation for Inventory Value Based on Personal Price Notes; configuration/state only, runtime integration is not implemented here. */
public class InventoryValueCalculator {
    public boolean enabled = true;
    public String pricesJson = "{}";
    public boolean includeShulkerContents = false;
}
