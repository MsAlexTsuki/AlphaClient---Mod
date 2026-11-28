package com.alpharip.staffmod.staffops.ideas;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Locale;

/** Public General HUD showing the distance at the moment either player lands a hit. */
public final class DistanceTrackerMod {
    private static final long DISPLAY_MILLIS = 3000L;

    private static double youDistance;
    private static double themDistance;
    private static long youUntil;
    private static long themUntil;

    private static boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("distance-tracker");
        return module != null && module.isEnabled();
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled() || mc == null || mc.thePlayer == null || mc.theWorld == null
                || event == null || event.entityLiving == null || event.source == null) {
            return;
        }

        Entity attacker = event.source.getEntity();
        if (!(attacker instanceof EntityPlayer) || attacker == event.entityLiving) {
            return;
        }

        long until = System.currentTimeMillis() + DISPLAY_MILLIS;

        // "You" updates only when the local player actually damages another player.
        if (attacker == mc.thePlayer && event.entityLiving instanceof EntityPlayer) {
            youDistance = mc.thePlayer.getDistanceToEntity(event.entityLiving);
            youUntil = until;
        }

        // "Them" updates only when another player actually damages the local player.
        if (event.entityLiving == mc.thePlayer && attacker instanceof EntityPlayer) {
            themDistance = mc.thePlayer.getDistanceToEntity(attacker);
            themUntil = until;
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled() || mc == null || mc.thePlayer == null || mc.currentScreen != null
                || mc.gameSettings == null || mc.gameSettings.showDebugInfo) {
            return;
        }

        long now = System.currentTimeMillis();
        String you = now < youUntil ? String.format(Locale.ROOT, "%.1f", youDistance) : "0";
        String them = now < themUntil ? String.format(Locale.ROOT, "%.1f", themDistance) : "0";

        mc.fontRendererObj.drawStringWithShadow("You: " + you, 5, 42, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Them: " + them, 5, 53, 0xFFFFFFFF);
    }
}
