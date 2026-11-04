// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.profile.ModuleProfileManager;

/**
 * S-Tap configuration. S-Tap deliberately uses the W key; WTapAssistance owns
 * the shared input handler so W-Tap and S-Tap cannot fight over key state.
 */
public final class STapAssistance {
    private static boolean requireW = true;
    private static int delay = 2;
    private static int cooldown = 4;
    private static int mode;
    private static int trigger;
    private static int target;

    private STapAssistance() {}

    private static boolean hasFounderAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                || SupabaseCommunityService.isSignedInFounder()
                || PermissionService.hasModAccess("w-tap-assistance")
                || PermissionService.hasCommandPermission("command.wtap", false);
    }

    private static boolean isConfiguredEnabled() {
        AlphaModule module = AlphaModuleRegistry.find("s-tap-assistance");
        return module != null && module.isConfiguredEnabled();
    }

    public static boolean isEnabled() {
        return isConfiguredEnabled() && hasFounderAccess();
    }

    public static void setEnabled(boolean value) {
        if (!hasFounderAccess()) return;
        AlphaModule module = AlphaModuleRegistry.find("s-tap-assistance");
        if (module == null) return;
        module.restoreEnabledState(value);
        ModuleProfileManager.setSelectedModuleEnabled(module.getId(), value);
    }

    public static boolean isRequireW() { return requireW; }
    public static void setRequireW(boolean value) {
        if (hasFounderAccess()) requireW = value;
    }

    public static int getDelay() { return delay; }
    public static void setDelay(int value) {
        if (hasFounderAccess()) delay = Math.max(0, Math.min(10, value));
    }

    public static int getCooldown() { return cooldown; }
    public static void setCooldown(int value) {
        if (hasFounderAccess()) cooldown = Math.max(0, Math.min(20, value));
    }

    public static int getMode() { return mode; }
    public static void setMode(int value) {
        if (hasFounderAccess()) mode = Math.max(0, Math.min(3, value));
    }

    public static int getTrigger() { return trigger; }
    public static void setTrigger(int value) {
        if (hasFounderAccess()) trigger = Math.max(0, Math.min(1, value));
    }

    public static int getTarget() { return target; }
    public static void setTarget(int value) {
        if (hasFounderAccess()) target = Math.max(0, Math.min(1, value));
    }
}
