package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Measures the distance from the player's eyes to the current crosshair hit. */
public final class StaffReachInspector {
    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-reach-inspector");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.fontRendererObj == null
                || mc.gameSettings.showDebugInfo || mc.objectMouseOver == null
                || mc.objectMouseOver.hitVec == null) return;
        double distance = mc.thePlayer.getPositionEyes(1.0F).distanceTo(mc.objectMouseOver.hitVec);
        String target = mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                ? "Block" : mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                ? "Entity" : "Hit";
        mc.fontRendererObj.drawStringWithShadow("Reach Inspector: " + target, 6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(String.format("Eye-to-hit distance: %.3f blocks", distance),
                6, 18, 0xFFFFFFFF);
    }
}
