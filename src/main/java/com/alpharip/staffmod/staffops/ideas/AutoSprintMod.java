package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * First working Auto Sprint implementation for Minecraft 1.8.9.
 * It only controls sprint while the public module is enabled and unlocked.
 */
public final class AutoSprintMod {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null) return;

        AlphaModule module = AlphaModuleRegistry.find("auto-sprint");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        if (mc.currentScreen != null || mc.thePlayer.isRiding()
                || mc.thePlayer.isSneaking() || mc.thePlayer.isUsingItem()
                || mc.thePlayer.isCollidedHorizontally
                || mc.thePlayer.moveForward <= 0.0F
                || mc.thePlayer.getFoodStats().getFoodLevel() <= 6) {
            return;
        }

        mc.thePlayer.setSprinting(true);
    }
}
