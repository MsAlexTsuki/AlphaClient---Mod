package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Shows the player's vanilla attack cooldown progress as a percentage. */
public final class AttackIndicatorMod {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("attack-indicator");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        int percent = Math.max(0, Math.min(100, Math.round(mc.thePlayer.getCooledAttackStrength(0.0F) * 100.0F)));
        mc.fontRendererObj.drawStringWithShadow("Attack: " + percent + "%", 6, 138, 0xFFFFFFFF);
    }
}
