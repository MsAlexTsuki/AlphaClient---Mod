package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Shows the held item's display name and remaining durability. */
public final class ItemHud {
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) return;
        AlphaModule module = AlphaModuleRegistry.find("item-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null) return;
        String line = held.getDisplayName();
        if (held.isItemStackDamageable()) {
            int remaining = Math.max(0, held.getMaxDamage() - held.getItemDamage());
            line += "  " + remaining + "/" + held.getMaxDamage();
        }
        float scale = HudScaleConfig.get("item-hud");
        GlStateManager.pushMatrix();
        GlStateManager.translate(6.0F, 78.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        mc.fontRendererObj.drawStringWithShadow(line, 0, 0, 0xFFFFFFFF);
        GlStateManager.popMatrix();
    }
}
