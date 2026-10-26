package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Displays identity, health, movement, and combat state for the targeted living mob. */
public final class StaffMobInspector {
    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-mob-inspector");
        return module != null && module.isEnabled() && module.isUnlocked();
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !enabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.fontRendererObj == null
                || mc.gameSettings.showDebugInfo || mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) return;
        Entity target = mc.objectMouseOver.entityHit;
        if (!(target instanceof EntityLivingBase) || target instanceof net.minecraft.entity.player.EntityPlayer) return;
        EntityLivingBase mob = (EntityLivingBase) target;
        mc.fontRendererObj.drawStringWithShadow("Mob: " + mob.getName() + " | " + mob.getClass().getSimpleName(),
                6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(String.format("Health: %.1f/%.1f | Hurt time: %d",
                mob.getHealth(), mob.getMaxHealth(), mob.hurtTime), 6, 18, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow(String.format("Position: %.1f, %.1f, %.1f",
                mob.posX, mob.posY, mob.posZ), 6, 30, 0xFFFFFFFF);
    }
}
