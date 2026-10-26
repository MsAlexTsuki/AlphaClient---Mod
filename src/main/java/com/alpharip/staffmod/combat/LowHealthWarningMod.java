package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Draws a simple low-health HUD warning without sending repeated chat messages. */
public final class LowHealthWarningMod {
    private boolean warningShown;

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) {
            warningShown = false;
            return;
        }
        AlphaModule module = AlphaModuleRegistry.find("low-health-warning");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) {
            warningShown = false;
            return;
        }

        float health = mc.thePlayer.getHealth();
        if (health <= 6.0F) {
            mc.fontRendererObj.drawStringWithShadow("LOW HEALTH", 6, 6, 0xFFFF5555);
            warningShown = true;
        } else {
            warningShown = false;
        }
    }
}
