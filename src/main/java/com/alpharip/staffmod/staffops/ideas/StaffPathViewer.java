package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Displays direct-route information to the entity under the crosshair. */
public final class StaffPathViewer {
    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-path-viewer");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.fontRendererObj == null
                || mc.gameSettings.showDebugInfo || mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY
                || mc.objectMouseOver.entityHit == null) return;
        Entity target = mc.objectMouseOver.entityHit;
        double dx = target.posX - mc.thePlayer.posX;
        double dy = target.posY - mc.thePlayer.posY;
        double dz = target.posZ - mc.thePlayer.posZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        boolean clear = mc.thePlayer.canEntityBeSeen(target);
        mc.fontRendererObj.drawStringWithShadow("Direct path to " + target.getName(), 6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(String.format("Distance %.2f | Delta %.2f, %.2f, %.2f",
                distance, dx, dy, dz), 6, 18, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Line of sight: " + (clear ? "clear" : "blocked"),
                6, 30, clear ? 0xFF55FF55 : 0xFFFF5555);
    }
}
