package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Minecraft 1.8.9 has no later-version attack cooldown meter. This first pass
 * reports the local hand-swing state rather than pretending to know server hit timing.
 */
public final class AttackTimingMod {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        AlphaModule module = AlphaModuleRegistry.find("attack-timing");
        Minecraft mc = Minecraft.getMinecraft();
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()
                || mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) return;

        boolean swinging = mc.thePlayer.isSwingInProgress;
        String text = swinging ? "Attack Timing: SWINGING" : "Attack Timing: READY";
        int color = swinging ? 0xFFFFD866 : 0xFF9CE2AC;
        mc.fontRendererObj.drawStringWithShadow(text, 6, 18, color);
    }
}
