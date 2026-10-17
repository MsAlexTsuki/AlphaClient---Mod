package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
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
    private boolean bobbingOverridden;

    /**
     * Legacy use-and-swing input: while right mouse is held and Minecraft is
     * actively using an item, preserve the left-click hand swing. Bows may swing
     * without targeting a block; other active-use items require a block hit so
     * clicking air does not trigger the legacy blockhit animation.
     * This adds a local swing animation; it does not cancel item use or guarantee
     * a server-side attack while the item is being consumed/drawn.
     */
    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (event == null || event.button != 0 || !event.buttonstate || !Mouse.isButtonDown(1)) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen != null || mc.thePlayer == null || mc.theWorld == null) return;
        if (!mc.thePlayer.isUsingItem()) return;

        ItemStack activeStack = mc.thePlayer.getItemInUse();
        boolean usingBow = activeStack != null && activeStack.getItem() instanceof ItemBow;
        if (!usingBow && (mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)) return;

        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        if (module == null || !module.isImplemented() || !module.isUnlocked()
                || !module.isEnabled() || !LegacyPvpSettings.isBlockhitEnabled()) return;

        // Preserve the visible swing without cancelling or interrupting item use.
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
