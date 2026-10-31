package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Lists active potion effects with ten seconds or less remaining. */
public final class PotionExpirationWarningMod {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("potion-expiration-warning");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        int row = 0;
        for (Object value : mc.thePlayer.getActivePotionEffects()) {
            if (!(value instanceof PotionEffect)) continue;
            PotionEffect effect = (PotionEffect) value;
            if (effect.getDuration() > 200) continue;
            int id = effect.getPotionID();
            if (id < 0 || id >= Potion.potionTypes.length) continue;
            Potion potion = Potion.potionTypes[id];
            if (potion == null) continue;
            String name = net.minecraft.util.StatCollector.translateToLocal(potion.getName());
            String time = Potion.getDurationString(effect);
            mc.fontRendererObj.drawStringWithShadow(name + " expires in " + time, 6, 20 + row * 10, 0xFFFFAA55);
            row++;
        }
    }
}
