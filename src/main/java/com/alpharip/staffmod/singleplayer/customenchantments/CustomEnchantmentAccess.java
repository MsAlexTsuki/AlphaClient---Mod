package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.client.Minecraft;

/** Keeps all AlphaClient custom-enchantment behaviour restricted to integrated Singleplayer. */
public final class CustomEnchantmentAccess {
    private CustomEnchantmentAccess() { }

    public static boolean isAvailable() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc != null && mc.isSingleplayer() && mc.thePlayer != null
                && mc.theWorld != null && mc.getIntegratedServer() != null;
    }
}
