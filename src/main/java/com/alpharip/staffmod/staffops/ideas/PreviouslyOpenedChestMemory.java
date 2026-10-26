package com.alpharip.staffmod.staffops.ideas;

/** Foundation for Searchable Memory of Previously Opened Chests; configuration/state only, runtime integration is not implemented here. */
public class PreviouslyOpenedChestMemory {
    public boolean enabled = true;
    public String searchQuery = "";
    public int maximumRememberedChests = 100;
}
