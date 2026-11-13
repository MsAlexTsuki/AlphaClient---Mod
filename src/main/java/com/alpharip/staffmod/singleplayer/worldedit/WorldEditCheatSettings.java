// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.entity.player.EntityPlayer;

/** Shared authorization state for both WorldEdit screens. */
public final class WorldEditCheatSettings {
    private static volatile boolean cheatsEnabled;

    private WorldEditCheatSettings() {
    }

    public static boolean areCheatsEnabled() {
        return cheatsEnabled && PermissionService.hasRogueOrHigherWorldEditAccess();
    }

    /** Enabling is refused unless the current account has a verified Rogue-or-higher rank. */
    public static boolean setCheatsEnabled(boolean enabled) {
        if (enabled && !PermissionService.hasRogueOrHigherWorldEditAccess()) return false;
        cheatsEnabled = enabled;
        return true;
    }

    /** Creative remains available through the normal WorldEdit GUI; Survival requires Rogue+ authorization. */
    public static boolean canUseInCurrentGameMode(EntityPlayer player) {
        if (player == null) return false;
        if (player.capabilities.isCreativeMode) return true;
        return areCheatsEnabled();
    }
}
