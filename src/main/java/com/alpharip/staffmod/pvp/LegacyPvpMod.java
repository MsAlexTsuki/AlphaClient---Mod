package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Client-side controller for the optional legacy PvP presentation mode.
 *
 * This initial implementation applies only the safe camera-bobbing preference.
 * Combat timing and knockback remain controlled by Minecraft/the server and are
 * not changed by this class.
 */
public final class LegacyPvpMod {
    private boolean wasEnabled;
    private boolean previousViewBobbing;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return;

        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        boolean enabled = module != null && module.isImplemented()
                && module.isUnlocked() && module.isEnabled()
                && LegacyPvpSettings.isDisableViewBobbing();

        if (enabled && !wasEnabled) {
            previousViewBobbing = mc.gameSettings.viewBobbing;
            mc.gameSettings.viewBobbing = false;
        } else if (enabled) {
            // Keep the legacy presentation consistent while the module is enabled.
            mc.gameSettings.viewBobbing = false;
        } else if (wasEnabled) {
            // Restore the user's previous option instead of permanently changing it.
            mc.gameSettings.viewBobbing = previousViewBobbing;
        }

        wasEnabled = enabled;
    }
}
