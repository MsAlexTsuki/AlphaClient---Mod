package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Inspects the villager selected by the crosshair. */
public final class StaffVillagerInspector {
    private boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("staff-villager-inspector");
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
        if (!(target instanceof EntityVillager)) return;
        EntityVillager villager = (EntityVillager) target;
        mc.fontRendererObj.drawStringWithShadow("Villager Inspector", 6, 6, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Profession: " + villager.getProfession()
                + " | Career: " + villager.getCareerId(), 6, 18, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Trading: " + (villager.isTrading() ? "yes" : "no")
                + " | Age: " + villager.getGrowingAge(), 6, 30, 0xFFFFFFFF);
    }
}
