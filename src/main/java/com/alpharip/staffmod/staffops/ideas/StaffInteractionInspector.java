package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Shows the type and details of the current crosshair interaction target. */
public final class StaffInteractionInspector {
    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-interaction-inspector");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.fontRendererObj == null
                || mc.gameSettings.showDebugInfo || mc.objectMouseOver == null) return;
        MovingObjectPosition hit = mc.objectMouseOver;
        String type = String.valueOf(hit.typeOfHit);
        String detail = "No target";
        if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && hit.getBlockPos() != null) {
            detail = hit.getBlockPos().getX() + ", " + hit.getBlockPos().getY() + ", " + hit.getBlockPos().getZ()
                    + (hit.sideHit == null ? "" : " | face " + hit.sideHit);
        } else if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hit.entityHit != null) {
            detail = hit.entityHit.getName() + " | entity ID " + hit.entityHit.getEntityId();
        }
        mc.fontRendererObj.drawStringWithShadow("Interaction target: " + type, 6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(detail, 6, 18, 0xFFFFFFFF);
    }
}
