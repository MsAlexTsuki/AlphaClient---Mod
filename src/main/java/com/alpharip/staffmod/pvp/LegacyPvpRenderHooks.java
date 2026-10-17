package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

/** Runtime gates for the narrowly scoped Legacy PvP render transformer. */
public final class LegacyPvpRenderHooks {
    private LegacyPvpRenderHooks() { }

    /**
     * Supplies swing progress only for an actual sword blockhit:
     * the module is enabled, right-click is held, the player is using a sword,
     * a block is targeted, and an attack swing is already in progress.
     * Holding right-click alone therefore never creates repeated blockhit swings.
     */
    public static float getBlockhitSwingProgress(float swingProgress) {
        if (swingProgress <= 0.0F) return 0.0F;

        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || mc.theWorld == null
                    || mc.currentScreen != null || !Mouse.isButtonDown(1)) {
                return 0.0F;
            }

            AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
            if (module == null || !module.isImplemented() || !module.isUnlocked()
                    || !module.isEnabled() || !LegacyPvpSettings.isBlockhitEnabled()) {
                return 0.0F;
            }

            ItemStack usingItem = mc.thePlayer.getItemInUse();
            if (usingItem == null || !(usingItem.getItem() instanceof ItemSword)) {
                return 0.0F;
            }

            MovingObjectPosition target = mc.objectMouseOver;
            if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return 0.0F;
            }

            return swingProgress;
        } catch (RuntimeException error) {
            System.err.println("[AlphaClient] Could not read Legacy PvP blockhit state: " + error.getMessage());
            return 0.0F;
        }
    }
}
