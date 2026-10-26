package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Displays a cardinal direction derived from the player's yaw. */
public final class CompassHudModule {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("compass-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        int direction = Math.floorMod((int) Math.floor(mc.thePlayer.rotationYaw * 4.0F / 360.0F + 0.5D), 4);
        String[] directions = {"S", "W", "N", "E"};
        mc.fontRendererObj.drawStringWithShadow("Facing: " + directions[direction], 6, 78, 0xFFFFFFFF);
    }
}
