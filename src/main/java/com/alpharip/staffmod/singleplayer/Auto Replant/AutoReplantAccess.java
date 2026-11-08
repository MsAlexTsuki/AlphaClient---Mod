package com.alpharip.staffmod.singleplayer.autoreplant;

import net.minecraft.client.Minecraft;

/** Restricts Auto Replant to an active integrated singleplayer world. */
public final class AutoReplantAccess {
    private AutoReplantAccess() { }

    public static boolean isAvailable(Minecraft mc) {
        return mc != null && mc.isSingleplayer() && mc.getIntegratedServer() != null
                && mc.thePlayer != null && mc.theWorld != null;
    }
}
