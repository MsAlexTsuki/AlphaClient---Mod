package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Displays the crosshair-targeted entity distance in blocks. */
public final class TargetDistanceHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("target-distance-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null) return;

        Entity target = mc.objectMouseOver.entityHit;
        double distance = mc.thePlayer.getDistanceToEntity(target);
        mc.fontRendererObj.drawStringWithShadow(String.format("Target distance: %.1f m", distance), 6, 30, 0xFFFFFFFF);
    }
}
