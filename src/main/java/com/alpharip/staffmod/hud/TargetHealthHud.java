package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Shows current and maximum health for a living entity under the crosshair. */
public final class TargetHealthHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("target-health-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null) return;
        Entity entity = mc.objectMouseOver.entityHit;
        if (!(entity instanceof EntityLivingBase)) return;
        EntityLivingBase living = (EntityLivingBase) entity;
        String text = String.format("%s: %.1f/%.1f HP", living.getName(), living.getHealth(), living.getMaxHealth());
        mc.fontRendererObj.drawStringWithShadow(text, 6, 126, 0xFFFFFFFF);
    }
}
