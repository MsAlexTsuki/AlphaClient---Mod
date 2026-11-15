package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Displays a warning when any equipped armor piece has 10% or less durability. */
public final class ArmorDurabilityWarningMod {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("armor-durability-warning");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        int lowPieces = 0;
        for (ItemStack stack : mc.thePlayer.inventory.armorInventory) {
            if (stack != null && stack.isItemStackDamageable()) {
                int remaining = stack.getMaxDamage() - stack.getItemDamage();
                if (remaining <= Math.max(1, stack.getMaxDamage() / 10)) lowPieces++;
            }
        }
        if (lowPieces > 0) {
            mc.fontRendererObj.drawStringWithShadow("LOW ARMOR DURABILITY: " + lowPieces, 6, 18, 0xFFFF5555);
        }
    }
}
