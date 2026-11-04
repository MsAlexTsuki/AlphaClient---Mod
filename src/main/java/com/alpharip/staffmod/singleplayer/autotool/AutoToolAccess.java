package com.alpharip.staffmod.singleplayer.autotool;

import net.minecraft.client.Minecraft;

/** Central access check for the singleplayer-only AutoTool feature. */
public final class AutoToolAccess {
    private AutoToolAccess() { }

    public static boolean isAvailable(Minecraft mc) {
        return mc != null
                && mc.isSingleplayer()
                && mc.thePlayer != null
                && mc.theWorld != null
                && mc.getIntegratedServer() != null;
    }
}
