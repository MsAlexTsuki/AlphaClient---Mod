package com.alpharip.staffmod.singleplayer.treeexcavate;

import net.minecraft.client.Minecraft;

/** Restricts Tree Excavate to an active integrated singleplayer world. */
public final class TreeExcavateAccess {
    private TreeExcavateAccess() { }

    public static boolean isAvailable(Minecraft mc) {
        return mc != null && mc.isSingleplayer() && mc.getIntegratedServer() != null
                && mc.thePlayer != null && mc.theWorld != null;
    }
}
