// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.quality.enchantingname;

import net.minecraft.client.gui.GuiEnchantment;

/** Runtime gate used by the GuiEnchantment hover transformer. */
public final class EnchantingNameHooks {
    private EnchantingNameHooks() {
    }

    public static boolean shouldSuppressHover(Object screen) {
        return screen instanceof GuiEnchantment && EnchantingNameMod.isEnabled();
    }
}
