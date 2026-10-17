package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
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
    private boolean pendingUseSwing;
    private boolean pendingPotionPunchSwing;
    private boolean previousViewBobbing;
    private boolean bobbingOverridden;

    /**
     * Legacy use-and-swing input: while right mouse is held and Minecraft is
     * actively using an item, preserve the left-click hand swing. This includes
     * bows, food, and potions; item-use animations must also work while targeting air.
     * This adds a local swing animation; it does not cancel item use or guarantee
     * a server-side attack while the item is being consumed/drawn.
     */
    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (event == null || event.button != 0 || !event.buttonstate || !Mouse.isButtonDown(1)) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen != null || mc.thePlayer == null || mc.theWorld == null) return;
        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) return;

        ItemStack held = mc.thePlayer.getHeldItem();
        if (held != null && held.getItem() instanceof ItemPotion
                && ItemPotion.isSplash(held.getMetadata())
                && LegacyPvpSettings.isPotionPunchAnimationEnabled()) {
            // Splash potions are thrown immediately on right-click, so isUsingItem()
            // is false for them. Queue only a local swing; never cancel either click event.
            pendingPotionPunchSwing = true;
            return;
        }

        if (!mc.thePlayer.isUsingItem() || !LegacyPvpSettings.isBlockhitEnabled()) return;

        // Defer the extra animation until the normal mouse processing has finished.
        // This avoids restarting a swing that vanilla already began for the same click.
        pendingUseSwing = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) return;

        if (pendingPotionPunchSwing) {
            pendingPotionPunchSwing = false;
            if (mc.currentScreen == null && mc.thePlayer != null && mc.theWorld != null
                    && Mouse.isButtonDown(1)) {
                // Restart the local arm swing for the legacy potion-punch pose,
                // even if vanilla already started the normal attack swing this tick.
                mc.thePlayer.swingItem();
            }
        }

        if (pendingUseSwing) {
            pendingUseSwing = false;
            if (mc.currentScreen == null && mc.thePlayer != null && mc.theWorld != null
                    && Mouse.isButtonDown(1) && mc.thePlayer.isUsingItem()) {
                if (!mc.thePlayer.isSwingInProgress) {
                    // Do not restart an active swing: it makes the arm snap into the bow-draw pose.
                    mc.thePlayer.swingItem();
                }
            }
        }

        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        boolean enabled = module != null && module.isImplemented()
                && module.isUnlocked() && module.isEnabled()
                && LegacyPvpSettings.isDisableViewBobbing();

        if (enabled) {
            if (!bobbingOverridden) {
                // Capture the real user setting once, before overriding it.
                previousViewBobbing = mc.gameSettings.viewBobbing;
                bobbingOverridden = true;
            }
            mc.gameSettings.viewBobbing = false;
        } else if (bobbingOverridden) {
            // Restore the original setting even if the option was toggled off
            // before the next tick or the module was disabled from another GUI.
            mc.gameSettings.viewBobbing = previousViewBobbing;
            bobbingOverridden = false;
        }

        wasEnabled = enabled;
    }
}
