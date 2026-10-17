package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.input.Mouse;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Client-side controller for the optional legacy PvP presentation mode.
 *
 * This initial implementation applies only the safe camera-bobbing preference.
 * Combat timing and knockback remain controlled by Minecraft/the server and are
 * not changed by this class.
 */
public final class LegacyPvpMod {
    private boolean wasEnabled;
    private boolean previousViewBobbing;

    /**
     * 1.7-style blockhit input: Minecraft 1.8.9 discards attack presses while
     * the player is using an item. When the sword is actively being used to
     * block, preserve the left-click swing so the hand can swing through while
     * right mouse remains held. This is a local animation/input behavior only.
     */
    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (event == null || event.button != 0 || !event.buttonstate || !Mouse.isButtonDown(1)) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen != null || mc.thePlayer == null || mc.theWorld == null) return;
        if (!mc.thePlayer.isUsingItem()) return;

        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemSword)) return;

        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        if (module == null || !module.isImplemented() || !module.isUnlocked()
                || !module.isEnabled() || !LegacyPvpSettings.isBlockhitEnabled()) return;

        // Vanilla skips clickMouse while using an item; trigger the swing that
        // would otherwise be swallowed, without cancelling or rewriting the click.
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
