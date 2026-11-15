package com.alpharip.staffmod.predictions.armor;

import net.minecraft.client.Minecraft;

/** Draws the compact warning HUD without changing the armor scan logic. */
public final class ArmorDurabilityWarningRenderer {
    public void render(Minecraft minecraft, ArmorDurabilityWarningResult result,
                       ArmorDurabilityWarningConfig config) {
        if (minecraft == null || minecraft.fontRendererObj == null
                || result == null || config == null || !result.hasWarnings()) {
            return;
        }

        minecraft.fontRendererObj.drawStringWithShadow(
                "LOW ARMOR DURABILITY: " + result.getLowDurabilityCount(),
                config.getHudX(),
                config.getHudY(),
                config.getHudColor());
    }
}
