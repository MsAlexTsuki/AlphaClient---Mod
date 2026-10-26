package com.alpharip.staffmod.staffops.ideas;

/** Foundation for Per-Type Render Limits for Armor Stands and Entities; configuration/state only, runtime integration is not implemented here. */
public class EntityTypeRenderLimits {
    public boolean enabled = true;
    public int armorStandLimit = 64;
    public int otherEntityLimit = 128;
}
