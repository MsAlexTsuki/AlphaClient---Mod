package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** First pass for a compact active-potion list. */
public final class PotionHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("potion-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        int row = 0;
        for (Object object : mc.thePlayer.getActivePotionEffects()) {
            if (!(object instanceof PotionEffect)) continue;
            PotionEffect effect = (PotionEffect) object;
            int id = effect.getPotionID();
            if (id < 0 || id >= Potion.potionTypes.length || Potion.potionTypes[id] == null) continue;
            String name = net.minecraft.util.StatCollector.translateToLocal(Potion.potionTypes[id].getName());
            mc.fontRendererObj.drawStringWithShadow(name + " " + Potion.getDurationString(effect), 6, 54 + row++ * 10, 0xFFFFFFFF);
        }
    }
}
