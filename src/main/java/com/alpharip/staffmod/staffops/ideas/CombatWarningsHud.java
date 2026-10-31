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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

public final class CombatWarningsHud {
    private static final String MODULE_ID = "combat-warnings";

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.fontRendererObj == null || mc.gameSettings.hideGUI
                || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find(MODULE_ID);
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        List<String> warnings = new ArrayList<String>();
        if (mc.thePlayer.getHealth() <= 6.0F) warnings.add("LOW HEALTH");
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held != null && held.isItemStackDamageable()) {
            int max = held.getMaxDamage();
            int remaining = Math.max(0, max - held.getItemDamage());
            if (remaining <= Math.max(5, max / 10)) warnings.add("LOW DURABILITY");
        }
        if (warnings.isEmpty()) return;

        float scale = HudScaleConfig.get(MODULE_ID);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(6.0F, 102.0F, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            int y = 0;
            for (String warning : warnings) {
                mc.fontRendererObj.drawStringWithShadow(warning, 0, y, 0xFFFF5555);
                y += mc.fontRendererObj.FONT_HEIGHT + 2;
            }
        } finally {
            GlStateManager.popMatrix();
        }
    }
}
