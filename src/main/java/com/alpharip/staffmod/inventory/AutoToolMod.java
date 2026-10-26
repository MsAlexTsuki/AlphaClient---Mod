package com.alpharip.staffmod.inventory;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

/**
 * First working Auto Tool pass. Selects the strongest hotbar tool for the
 * block being mined and restores the previous slot when the attack is released.
 */
public final class AutoToolMod {
    private int originalSlot = -1;
    private boolean changedSlot;

    @SubscribeEvent
    public void onBlockClick(PlayerInteractEvent event) {
        if (event.world == null || event.pos == null || event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || event.entityPlayer != mc.thePlayer) return;

        AlphaModule module = AlphaModuleRegistry.find("auto-tool");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;

        BlockPos pos = event.pos;
        Block block = event.world.getBlockState(pos).getBlock();
        int bestSlot = mc.thePlayer.inventory.currentItem;
        float bestStrength = strength(mc.thePlayer.inventory.getStackInSlot(bestSlot), block);

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            float candidate = strength(stack, block);
            if (candidate > bestStrength) {
                bestStrength = candidate;
                bestSlot = slot;
            }
        }

        if (bestSlot != mc.thePlayer.inventory.currentItem) {
            if (!changedSlot) originalSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = bestSlot;
            changedSlot = true;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !changedSlot || Mouse.isButtonDown(0)) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null && originalSlot >= 0 && originalSlot < 9) {
            mc.thePlayer.inventory.currentItem = originalSlot;
        }
        originalSlot = -1;
        changedSlot = false;
    }

    private static float strength(ItemStack stack, Block block) {
        if (stack == null) return 1.0F;
        return stack.getStrVsBlock(block);
    }
}
