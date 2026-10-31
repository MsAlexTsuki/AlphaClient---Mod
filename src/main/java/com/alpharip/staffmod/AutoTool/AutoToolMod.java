package com.alpharip.staffmod.AutoTool;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Selects the strongest available hotbar tool when the local player starts mining a block.
 * The feature only changes the selected hotbar slot; Minecraft synchronizes that selection normally.
 */
public final class AutoToolMod {
    private int originalSlot = -1;
    private int autoSelectedSlot = -1;
    private EntityPlayer sessionPlayer;
    private World sessionWorld;
    private int ticksSinceSwitch;

    @SubscribeEvent
    public void onBlockClick(PlayerInteractEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event == null || event.isCanceled()
                || event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
                || !AutoToolSettings.isEnabled()
                || mc == null || mc.thePlayer == null
                || event.entityPlayer != mc.thePlayer
                || mc.theWorld == null || event.world != mc.theWorld
                || event.pos == null) {
            return;
        }

        EntityPlayer player = mc.thePlayer;
        if (sessionPlayer == player && player.inventory.currentItem != autoSelectedSlot) {
            clearSession();
        }

        IBlockState state = event.world.getBlockState(event.pos);
        if (state == null || state.getBlock() == null) return;
        Block block = state.getBlock();
        int currentSlot = player.inventory.currentItem;
        ItemStack currentStack = player.inventory.getStackInSlot(currentSlot);
        float currentStrength = strength(currentStack, block);
        int bestSlot = currentSlot;
        float bestStrength = currentStrength;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack candidate = player.inventory.getStackInSlot(slot);
            float candidateStrength = strength(candidate, block);
            if (candidateStrength > bestStrength + 0.001F) {
                bestStrength = candidateStrength;
                bestSlot = slot;
            }
        }

        // Do not switch to a random item when no hotbar stack is better than the held item.
        if (bestSlot == currentSlot) return;

        if (originalSlot < 0 || sessionPlayer != player || sessionWorld != event.world) {
            originalSlot = currentSlot;
        }
        player.inventory.currentItem = bestSlot;
        autoSelectedSlot = bestSlot;
        ticksSinceSwitch = 0;
        sessionPlayer = player;
        sessionWorld = event.world;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END || sessionPlayer == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer != sessionPlayer || mc.theWorld != sessionWorld
                || mc.gameSettings == null) {
            clearSession();
            return;
        }

        if (sessionPlayer.inventory.currentItem != autoSelectedSlot) {
            // The player selected another slot manually; do not override that choice.
            clearSession();
            return;
        }
        ticksSinceSwitch++;

        boolean finishedMining = (!mc.gameSettings.keyBindAttack.isKeyDown() && ticksSinceSwitch >= 2)
                || mc.currentScreen != null || !AutoToolSettings.isEnabled();
        if (!finishedMining) return;

        if (AutoToolSettings.isSwapBackEnabled()
                && originalSlot >= 0 && originalSlot < 9
                && sessionPlayer.inventory.currentItem == autoSelectedSlot) {
            sessionPlayer.inventory.currentItem = originalSlot;
        }
        clearSession();
    }

    private static float strength(ItemStack stack, Block block) {
        if (stack == null || stack.stackSize <= 0 || block == null) return 0.0F;
        try {
            return stack.getStrVsBlock(block);
        } catch (RuntimeException invalidItem) {
            return 0.0F;
        }
    }

    private void clearSession() {
        originalSlot = -1;
        autoSelectedSlot = -1;
        sessionPlayer = null;
        sessionWorld = null;
        ticksSinceSwitch = 0;
    }
}
