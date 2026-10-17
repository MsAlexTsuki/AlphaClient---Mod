package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;

/** Runtime gates for the narrowly scoped Legacy PvP render transformer. */
public final class LegacyPvpRenderHooks {
    private LegacyPvpRenderHooks() { }

    /**
     * Returns the swing progress only while Legacy PvP and its blockhit option
     * are enabled. Returning zero preserves vanilla 1.8.9 blocking animation.
     */
    public static float getBlockhitSwingProgress(float swingProgress) {
        try {
            AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
            if (module != null && module.isImplemented() && module.isUnlocked()
                    && module.isEnabled() && LegacyPvpSettings.isBlockhitEnabled()) {
                return swingProgress;
            }
        } catch (RuntimeException error) {
            System.err.println("[AlphaClient] Could not read Legacy PvP blockhit state: " + error.getMessage());
        }
        return 0.0F;
    }
}
