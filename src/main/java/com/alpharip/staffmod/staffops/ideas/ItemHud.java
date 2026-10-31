package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Base rendering pattern for an AlphaClient 1.8.9 HUD.
 * Each HUD remains a separate module and uses its own registry ID and scale setting.
 */
import net.minecraft.item.ItemStack;

public final class ItemHud {
    private static final String MODULE_ID = "item-hud";

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.fontRendererObj == null || mc.gameSettings.hideGUI
                || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find(MODULE_ID);
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null) return;
        String text = held.getDisplayName();
        if (held.isItemStackDamageable()) {
            int max = held.getMaxDamage();
            int remaining = Math.max(0, max - held.getItemDamage());
            text += "  " + remaining + "/" + max;
        }

        float scale = HudScaleConfig.get(MODULE_ID);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(6.0F, 78.0F, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            mc.fontRendererObj.drawStringWithShadow(text, 0, 0, 0xFFFFFFFF);
        } finally {
            GlStateManager.popMatrix();
        }
    }
}
