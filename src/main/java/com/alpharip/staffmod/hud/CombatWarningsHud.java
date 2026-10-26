package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;

/** Warns locally about low health and nearly broken held equipment. */
public final class CombatWarningsHud {
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.gameSettings == null
                || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) return;
        AlphaModule module = AlphaModuleRegistry.find("combat-warnings");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        List<String> warnings = new ArrayList<String>();
        if (mc.thePlayer.getHealth() <= 6.0F) warnings.add("LOW HEALTH");
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held != null && held.isItemStackDamageable()
                && held.getMaxDamage() - held.getItemDamage() <= Math.max(5, held.getMaxDamage() / 10)) {
            warnings.add("LOW DURABILITY");
        }
        if (warnings.isEmpty()) return;
        float scale = HudScaleConfig.get("combat-warnings");
        GlStateManager.pushMatrix();
        GlStateManager.translate(6.0F, 102.0F, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        int y = 0;
        for (String warning : warnings) {
            mc.fontRendererObj.drawStringWithShadow(warning, 0, y, 0xFFFF5555);
            y += mc.fontRendererObj.FONT_HEIGHT + 2;
        }
        GlStateManager.popMatrix();
    }
}
