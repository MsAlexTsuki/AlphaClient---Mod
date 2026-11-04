package com.alpharip.staffmod.singleplayer.autotool;

import com.alpharip.staffmod.singleplayer.autotool.AutoToolAccess;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Locale;

/** Chooses and synchronizes a suitable hotbar tool for the targeted block. */
public final class AutoToolMod {
    private final KeyBinding pauseKey = new KeyBinding("AutoTool: Pause", Keyboard.KEY_P, "AlphaClient");
    private boolean pauseToggle;
    private int feedbackCooldown;
    private int originalSlot = -1;
    private int autoSelectedSlot = -1;
    private int ticksSinceSwitch;
    private int releaseTicks;
    private BlockPos sessionPos;
    private Block sessionBlock;
    private EntityPlayer sessionPlayer;
    private World sessionWorld;

    public AutoToolMod() {
        ClientRegistry.registerKeyBinding(pauseKey);
    }

    @SubscribeEvent
    public void onBlockClick(net.minecraftforge.event.entity.player.PlayerInteractEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event == null || event.isCanceled()
                || event.action != net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
                || !AutoToolAccess.isAvailable(mc)
                || !AutoToolSettings.isEnabled()
                || mc == null || mc.thePlayer == null
                || event.entityPlayer != mc.thePlayer
                || mc.theWorld == null || event.world != mc.theWorld
                || event.pos == null || isPaused(mc, mc.thePlayer)) return;

        IBlockState state = event.world.getBlockState(event.pos);
        if (state == null || state.getBlock() == null) return;

        EntityPlayer player = mc.thePlayer;
        Block block = state.getBlock();
        String blockId = registryId(Block.blockRegistry.getNameForObject(block));
        String preferredId = AutoToolSettings.getPreferredItem(blockId);
        int currentSlot = player.inventory.currentItem;
        Candidate best = null;
        boolean safeCandidateAvailable = false;
        Candidate[] candidates = new Candidate[9];

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            String itemId = stack == null ? "" : registryId(Item.itemRegistry.getNameForObject(stack.getItem()));
            if (stack != null && AutoToolSettings.isItemIgnored(itemId)) continue;

            Candidate candidate = new Candidate(slot, itemId, stack, block, itemId.equals(preferredId));
            candidates[slot] = candidate;
            if (candidate.canHarvest && candidate.durabilityPercent >= AutoToolSettings.getMinimumDurability()) {
                safeCandidateAvailable = true;
            }
        }

        best = candidates[currentSlot];
        for (Candidate candidate : candidates) {
            if (candidate != null && candidate != best
                    && (best == null || isBetter(candidate, best, safeCandidateAvailable))) {
                best = candidate;
            }
        }
        if (best == null) return;

        boolean hadSession = sessionPlayer == player && sessionWorld == event.world
                && player.inventory.currentItem == autoSelectedSlot;
        if (best.slot == currentSlot) {
            if (hadSession) {
                sessionPos = event.pos;
                sessionBlock = block;
                releaseTicks = 0;
                ticksSinceSwitch = 0;
            }
            return;
        }

        if (!hadSession) originalSlot = currentSlot;
        player.inventory.currentItem = best.slot;
        autoSelectedSlot = best.slot;
        sessionPlayer = player;
        sessionWorld = event.world;
        sessionPos = event.pos;
        sessionBlock = block;
        ticksSinceSwitch = 0;
        releaseTicks = 0;
        if (mc.playerController != null) mc.playerController.updateController();
        showSelectionFeedback(mc, best);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!AutoToolAccess.isAvailable(Minecraft.getMinecraft())) {
            pauseToggle = false;
            return;
        }
        while (pauseKey.isPressed()) {
            if (!AutoToolSettings.isPauseToggleMode()) continue;
            pauseToggle = !pauseToggle;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§6[AutoTool] §f"
                        + (pauseToggle ? "Paused." : "Resumed.")));
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event == null || event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!AutoToolAccess.isAvailable(mc)) {
            clearSession();
            pauseToggle = false;
            return;
        }
        if (feedbackCooldown > 0) feedbackCooldown--;
        if (sessionPlayer == null) return;
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
        boolean interrupted = mc.currentScreen != null || !AutoToolSettings.isEnabled();
        boolean finished = interrupted;
        String mode = AutoToolSettings.getSwapBackMode();

        if (!finished && "RELEASE".equals(mode)) {
            if (!mc.gameSettings.keyBindAttack.isKeyDown()) releaseTicks++;
            else releaseTicks = 0;
            finished = !mc.gameSettings.keyBindAttack.isKeyDown()
                    && releaseTicks >= AutoToolSettings.getSwapBackDelay();
        } else if (!finished && "BROKEN".equals(mode)) {
            finished = sessionPos != null && sessionWorld.isBlockLoaded(sessionPos)
                    && (sessionWorld.isAirBlock(sessionPos)
                    || sessionWorld.getBlockState(sessionPos).getBlock() != sessionBlock);
        } else if (!finished && "NEVER".equals(mode)) {
            finished = !mc.gameSettings.keyBindAttack.isKeyDown() && ticksSinceSwitch >= 2;
        }

        if (!finished && isPaused(mc, sessionPlayer)) finished = true;
        if (!finished) return;

        boolean shouldReturn = interrupted || !"NEVER".equals(mode);
        boolean changedSlot = shouldReturn && originalSlot >= 0 && originalSlot < 9
                && sessionPlayer.inventory.currentItem == autoSelectedSlot;
        if (changedSlot) sessionPlayer.inventory.currentItem = originalSlot;
        clearSession();
        if (changedSlot && mc.playerController != null) mc.playerController.updateController();
    }

    @SubscribeEvent
    public void renderPauseIndicator(RenderGameOverlayEvent.Post event) {
        if (event == null || event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!AutoToolAccess.isAvailable(mc) || !isPaused(mc, mc.thePlayer)) return;
        mc.fontRendererObj.drawStringWithShadow("AutoTool paused (" + Keyboard.getKeyName(pauseKey.getKeyCode()) + ")", 8, 8, 0xFFFFC83D);
    }

    private boolean isPaused(Minecraft mc, EntityPlayer player) {
        if (AutoToolSettings.isPauseToggleMode() ? pauseToggle : pauseKey.isKeyDown()) return true;
        if (AutoToolSettings.isPauseWhenUsingItem() && player.isUsingItem()) return true;
        if (AutoToolSettings.isPauseWhenSneaking() && player.isSneaking()) return true;
        if (mc.playerController == null) return false;
        WorldSettings.GameType mode = mc.playerController.getCurrentGameType();
        return (mode == WorldSettings.GameType.SURVIVAL && AutoToolSettings.isPauseInSurvival())
                || (mode == WorldSettings.GameType.CREATIVE && AutoToolSettings.isPauseInCreative())
                || (mode == WorldSettings.GameType.ADVENTURE && AutoToolSettings.isPauseInAdventure());
    }

    private void showSelectionFeedback(Minecraft mc, Candidate candidate) {
        if (!AutoToolSettings.isFeedbackEnabled() || feedbackCooldown > 0 || mc.thePlayer == null) return;
        String reason = candidate.preferred ? "preferred for this block"
                : candidate.canHarvest ? "can harvest this block" : "breaks this block faster";
        mc.thePlayer.addChatMessage(new ChatComponentText("§6[AutoTool] §fSelected "
                + candidate.displayName() + " §7(" + reason + ")"));
        feedbackCooldown = 20;
    }

    private static boolean isBetter(Candidate candidate, Candidate current, boolean safeAvailable) {
        if (candidate.canHarvest != current.canHarvest) return candidate.canHarvest;
        if (safeAvailable && candidate.canHarvest
                && (candidate.durabilityPercent >= AutoToolSettings.getMinimumDurability())
                != (current.durabilityPercent >= AutoToolSettings.getMinimumDurability())) {
            return candidate.durabilityPercent >= AutoToolSettings.getMinimumDurability();
        }
        if (candidate.preferred != current.preferred) return candidate.preferred;
        if (candidate.strength > current.strength + 0.001F) return true;
        if (current.strength > candidate.strength + 0.001F) return false;
        return AutoToolSettings.isDurabilityTieBreakEnabled()
                && candidate.durabilityPercent > current.durabilityPercent;
    }

    private static boolean canHarvest(ItemStack stack, Block block) {
        if (stack == null || stack.stackSize <= 0 || block == null) return false;
        try { return stack.canHarvestBlock(block); }
        catch (RuntimeException ignored) { return false; }
    }

    private static String registryId(ResourceLocation location) {
        return location == null ? "" : location.toString().toLowerCase(Locale.ROOT);
    }

    private void clearSession() {
        originalSlot = -1;
        autoSelectedSlot = -1;
        ticksSinceSwitch = 0;
        releaseTicks = 0;
        sessionPos = null;
        sessionBlock = null;
        sessionPlayer = null;
        sessionWorld = null;
    }

    private static final class Candidate {
        private final int slot;
        private final ItemStack stack;
        private final boolean canHarvest;
        private final boolean preferred;
        private final float strength;
        private final int durabilityPercent;

        private Candidate(int slot, String itemId, ItemStack stack, Block block, boolean preferred) {
            this.slot = slot;
            this.stack = stack;
            this.canHarvest = canHarvest(stack, block);
            this.preferred = preferred && this.canHarvest;
            float rawStrength = stack == null || stack.stackSize <= 0 ? 1.0F : getStrength(stack, block);
            if (AutoToolSettings.isEfficiencyAware() && stack != null && rawStrength > 1.0F) {
                int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
                if (efficiency > 0) rawStrength += efficiency * efficiency + 1.0F;
            }
            this.strength = rawStrength;
            this.durabilityPercent = remainingDurability(stack);
        }

        private String displayName() { return stack == null ? "empty hand" : stack.getDisplayName(); }
    }

    private static float getStrength(ItemStack stack, Block block) {
        if (stack == null || stack.stackSize <= 0 || block == null) return 1.0F;
        try {
            float strength = stack.getStrVsBlock(block);
            return Float.isNaN(strength) || Float.isInfinite(strength) || strength < 0.0F ? 1.0F : strength;
        } catch (RuntimeException ignored) {
            return 1.0F;
        }
    }

    private static int remainingDurability(ItemStack stack) {
        if (stack == null || !stack.isItemStackDamageable() || stack.getMaxDamage() <= 0) return 100;
        int remaining = Math.max(0, stack.getMaxDamage() - stack.getItemDamage());
        return (int) Math.floor(remaining * 100.0D / stack.getMaxDamage());
    }
}
