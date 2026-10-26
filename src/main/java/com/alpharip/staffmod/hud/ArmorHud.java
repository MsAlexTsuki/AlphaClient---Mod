package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Text-based first pass for armor status and durability. */
public final class ArmorHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("armor-hud");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        int row = 0;
        for (ItemStack stack : mc.thePlayer.inventory.armorInventory) {
            if (stack == null) continue;
            String label = stack.getDisplayName();
            if (stack.isItemStackDamageable()) {
                int remaining = Math.max(0, stack.getMaxDamage() - stack.getItemDamage());
                label += " " + remaining + "/" + stack.getMaxDamage();
            }
            mc.fontRendererObj.drawStringWithShadow(label, 6, 66 + row++ * 10, 0xFFFFFFFF);
        }
    }
}
