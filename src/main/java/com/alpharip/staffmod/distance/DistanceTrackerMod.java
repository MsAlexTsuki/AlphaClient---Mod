package com.alpharip.staffmod.distance;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

import java.util.Locale;

/** First version of the public General distance tracker. */
public final class DistanceTrackerMod {
    private static final long DISPLAY_MILLIS = 3000L;
    private static double youDistance;
    private static double themDistance;
    private static long youUntil;
    private static long themUntil;
    private static boolean loaded;

    private static boolean enabled() {
        AlphaModule module = AlphaModuleRegistry.find("distance-tracker");
        return module != null && module.isEnabled();
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled() || mc == null || mc.thePlayer == null || mc.theWorld == null
                || mc.currentScreen != null || !Mouse.getEventButtonState()
                || Mouse.getEventButton() != 0 || mc.objectMouseOver == null) return;
        Entity target = mc.objectMouseOver.entityHit;
        if (target instanceof EntityPlayer && target != mc.thePlayer) {
            youDistance = mc.thePlayer.getDistanceToEntity(target);
            youUntil = System.currentTimeMillis() + DISPLAY_MILLIS;
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled() || mc == null || mc.thePlayer == null || event.entityLiving != mc.thePlayer) return;
        Entity attacker = event.source == null ? null : event.source.getEntity();
        if (attacker instanceof EntityPlayer && attacker != mc.thePlayer) {
            themDistance = mc.thePlayer.getDistanceToEntity(attacker);
            themUntil = System.currentTimeMillis() + DISPLAY_MILLIS;
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled() || mc == null || mc.thePlayer == null || mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        String you = now < youUntil ? String.format(Locale.ROOT, "%.1f", youDistance) : "0";
        String them = now < themUntil ? String.format(Locale.ROOT, "%.1f", themDistance) : "0";
        mc.fontRendererObj.drawStringWithShadow("You: " + you, 5, 42, 0xFFFFFFFF);
        mc.fontRendererObj.drawStringWithShadow("Them: " + them, 5, 53, 0xFFFFFFFF);
    }
}
