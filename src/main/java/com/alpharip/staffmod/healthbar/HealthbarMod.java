package com.alpharip.staffmod.healthbar;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** First version: briefly draws a health bar above the player currently targeted by the crosshair. */
public final class HealthbarMod {
    private EntityPlayer target;
    private long visibleUntil;

    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("healthbar");
        return module != null && module.isEnabled()
                && PermissionService.hasStaffModsAccess();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled() || mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            target = null;
            visibleUntil = 0L;
            return;
        }
        MovingObjectPosition hit = mc.objectMouseOver;
        Entity entity = hit == null ? null : hit.entityHit;
        if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
            if (target != entity) {
                target = (EntityPlayer) entity;
                visibleUntil = System.currentTimeMillis() + 5000L;
            }
        } else if (System.currentTimeMillis() >= visibleUntil) {
            target = null;
        }
        if (target != null && (!target.isEntityAlive() || target.isDead
                || mc.theWorld.getEntityByID(target.getEntityId()) != target)) {
            target = null;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        // Rendering is intentionally isolated in a helper so the world-space drawing
        // can be refined without mixing target selection and lifetime logic.
        if (!enabled() || target == null || System.currentTimeMillis() >= visibleUntil) return;
        HealthbarWorldRenderer.render(target, event.partialTicks);
    }
}
