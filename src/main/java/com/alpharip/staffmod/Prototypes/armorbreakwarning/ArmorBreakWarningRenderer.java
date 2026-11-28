package com.alpharip.staffmod.armorbreakwarning;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Draws the warning when one or more equipped armor pieces approach breaking. */
public final class ArmorBreakWarningRenderer {
    private final ArmorBreakWarningSettings settings;

    ArmorBreakWarningRenderer(ArmorBreakWarningSettings settings) {
        this.settings = settings;
    }

    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!settings.isEnabled() || mc == null || mc.thePlayer == null
                || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) {
            return;
        }

        int lowPieces = countLowDurabilityPieces(mc.thePlayer.inventory.armorInventory);
        if (lowPieces > 0) {
            mc.fontRendererObj.drawStringWithShadow(
                    "LOW ARMOR DURABILITY: " + lowPieces, 6, 18, 0xFFFF5555);
        }
    }

    int countLowDurabilityPieces(ItemStack[] armorInventory) {
        if (armorInventory == null) return 0;

        int lowPieces = 0;
        int thresholdPercent = settings.getThresholdPercent();
        for (ItemStack stack : armorInventory) {
            if (stack == null || !stack.isItemStackDamageable()) continue;

            int maximum = stack.getMaxDamage();
            if (maximum <= 0) continue;

            int remaining = Math.max(0, maximum - stack.getItemDamage());
            int threshold = Math.max(1, (maximum * thresholdPercent) / 100);
            if (remaining <= threshold) {
                lowPieces++;
            }
        }
        return lowPieces;
    }
}
