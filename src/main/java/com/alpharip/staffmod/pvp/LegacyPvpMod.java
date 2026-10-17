package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.input.Mouse;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Client-side controller for Legacy PvP presentation and input behavior.
 * The blockhit swing is local animation/input handling; combat timing and
 * knockback remain controlled by Minecraft and the server.
 */
public final class LegacyPvpMod {
    private boolean wasEnabled;
    private boolean previousViewBobbing;

    /**
     * Legacy use-and-swing input: while right mouse is held and Minecraft is
     * actively using an item, preserve the left-click hand swing only when the
     * crosshair is targeting a block. This covers swords, food, bows, drinkable
     * potions and other active-use items. It does not trigger when clicking air.
     * This adds a local swing animation; it does not cancel item use or guarantee
     * a server-side attack while the item is being consumed/drawn.
     */
    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (event == null || event.button != 0 || !event.buttonstate || !Mouse.isButtonDown(1)) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen != null || mc.thePlayer == null || mc.theWorld == null) return;
        if (!mc.thePlayer.isUsingItem()) return;
        if (mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        if (module == null || !module.isImplemented() || !module.isUnlocked()
                || !module.isEnabled() || !LegacyPvpSettings.isBlockhitEnabled()) return;

        // Preserve the visible swing for any actively used item without
        // cancelling the original mouse event or interrupting its use.
        mc.thePlayer.swingItem();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return;

        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        boolean enabled = module != null && module.isImplemented()
                && module.isUnlocked() && module.isEnabled()
                && LegacyPvpSettings.isDisableViewBobbing();

        if (enabled && !wasEnabled) {
            previousViewBobbing = mc.gameSettings.viewBobbing;
            mc.gameSettings.viewBobbing = false;
        } else if (enabled) {
            // Keep the legacy presentation consistent while the module is enabled.
            mc.gameSettings.viewBobbing = false;
        } else if (wasEnabled) {
            // Restore the user's previous option instead of permanently changing it.
            mc.gameSettings.viewBobbing = previousViewBobbing;
        }

        wasEnabled = enabled;
    }
}
