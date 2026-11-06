// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Founder-only AimAssist runtime entry point.
 * Existing access checks and module toggle remain the authority for activation.
 */
public final class AimAssist {
    private final AimAssistController controller = new AimAssistController();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null
                || !SupabaseCommunityService.isSignedInFounder()) {
            controller.resetPendingTarget();
            return;
        }
        AlphaModule module = AlphaModuleRegistry.find("aim-assist");
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) {
            controller.resetPendingTarget();
            return;
        }
        controller.update(mc);
    }
}
