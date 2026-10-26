package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Counts block items in the player's main inventory and renders the total. */
public final class BlockCounterHud {
    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        AlphaModule module = AlphaModuleRegistry.find("block-counter");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        int total = 0;
        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof net.minecraft.item.ItemBlock) {
                total += stack.stackSize;
            }
        }
        mc.fontRendererObj.drawStringWithShadow("Blocks: " + total, 6, 6, 0xFFFFFFFF);
    }
}
