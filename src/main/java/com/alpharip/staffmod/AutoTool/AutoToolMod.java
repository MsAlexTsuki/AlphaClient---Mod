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
 * Selects the best hotbar tool when the local player starts mining a block.
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
        boolean currentCanHarvest = canHarvest(currentStack, block);
        float currentStrength = strength(currentStack, block);
        int bestSlot = currentSlot;
        boolean bestCanHarvest = currentCanHarvest;
        float bestStrength = currentStrength;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack candidate = player.inventory.getStackInSlot(slot);
            boolean candidateCanHarvest = canHarvest(candidate, block);
            float candidateStrength = strength(candidate, block);
            if (isBetter(candidateCanHarvest, candidateStrength, bestCanHarvest, bestStrength)) {
                bestCanHarvest = candidateCanHarvest;
                bestStrength = candidateStrength;
                bestSlot = slot;
            }
        }

        if (bestSlot == currentSlot) return;

        if (originalSlot < 0 || sessionPlayer != player || sessionWorld != event.world) {
            originalSlot = currentSlot;
        }
        player.inventory.currentItem = bestSlot;
        autoSelectedSlot = bestSlot;
        ticksSinceSwitch = 0;
        sessionPlayer = player;
        sessionWorld = event.world;

        // PlayerControllerMP synchronizes before raising this interaction event.
        // Synchronize again now, before vanilla sends START_DESTROY_BLOCK.
        if (mc.playerController != null) {
            mc.playerController.updateController();
        }
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
            clearSession();
            return;
        }
        ticksSinceSwitch++;

        boolean finishedMining = (!mc.gameSettings.keyBindAttack.isKeyDown() && ticksSinceSwitch >= 2)
                || mc.currentScreen != null || !AutoToolSettings.isEnabled();
        if (!finishedMining) return;

        boolean changedSlot = false;
        if (AutoToolSettings.isSwapBackEnabled()
                && originalSlot >= 0 && originalSlot < 9
                && sessionPlayer.inventory.currentItem == autoSelectedSlot) {
            sessionPlayer.inventory.currentItem = originalSlot;
            changedSlot = true;
        }
        clearSession();

        if (changedSlot && mc.playerController != null) {
            mc.playerController.updateController();
        }
    }

    private static boolean isBetter(boolean candidateCanHarvest, float candidateStrength,
                                    boolean currentCanHarvest, float currentStrength) {
        if (candidateCanHarvest != currentCanHarvest) return candidateCanHarvest;
        return candidateStrength > currentStrength + 0.001F;
    }

    private static boolean canHarvest(ItemStack stack, Block block) {
        if (stack == null || stack.stackSize <= 0 || block == null) return false;
        try {
            return stack.canHarvestBlock(block);
        } catch (RuntimeException invalidItem) {
            return false;
        }
    }

    private static float strength(ItemStack stack, Block block) {
        // The empty hand is the baseline, not a zero-strength tool.
        if (stack == null || stack.stackSize <= 0 || block == null) return 1.0F;
        try {
            float strength = stack.getStrVsBlock(block);
            return Float.isNaN(strength) || Float.isInfinite(strength) || strength < 0.0F
                    ? 1.0F : strength;
        } catch (RuntimeException invalidItem) {
            return 1.0F;
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
