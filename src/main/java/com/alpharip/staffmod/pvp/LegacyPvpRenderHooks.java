package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/** Runtime gates for the narrowly scoped Legacy PvP render transformer. */
public final class LegacyPvpRenderHooks {
    private static Field leftClickCounterField;
    private static boolean leftClickCounterLookupAttempted;
    private static boolean leftClickCounterLookupFailed;

    private LegacyPvpRenderHooks() { }

    /**
     * Removes only Minecraft's local miss-in-air delay while the opt-in
     * Legacy PvP setting is enabled. Looking at a block or entity preserves
     * vanilla click timing, and this never creates a blockhit animation.
     */
    public static void resetMissedSwingDelayIfNeeded() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
                return;
            }

            AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
            if (module == null || !module.isImplemented() || !module.isUnlocked()
                    || !module.isEnabled() || !LegacyPvpSettings.isRemoveMissedSwingDelayEnabled()) {
                return;
            }

            // During sword blockhit, vanilla can leave the local click counter
            // active after a previous click. That makes a fresh left-click appear
            // to do nothing while right-click is held. Clear that client-side gate
            // for the explicit left+right mouse combination, regardless of whether
            // the crosshair currently points at air, a block, or an entity.
            boolean blockhitCombo = Mouse.isButtonDown(0)
                    && Mouse.isButtonDown(1)
                    && LegacyPvpSettings.isBlockhitEnabled()
                    && mc.thePlayer.getHeldItem() != null
                    && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
            MovingObjectPosition target = mc.objectMouseOver;
            if (!blockhitCombo && target != null
                    && target.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
                return;
            }

            if (!leftClickCounterLookupAttempted) {
                leftClickCounterLookupAttempted = true;
                try {
                    leftClickCounterField = ReflectionHelper.findField(Minecraft.class,
                            "leftClickCounter", "field_71429_W");
                } catch (RuntimeException error) {
                    leftClickCounterLookupFailed = true;
                    System.err.println("[AlphaClient] Could not locate Minecraft leftClickCounter; missed-swing timing remains vanilla: " + error.getMessage());
                }
            }
            if (!leftClickCounterLookupFailed && leftClickCounterField != null) {
                leftClickCounterField.setInt(mc, 0);
            }
        } catch (ReflectiveOperationException | RuntimeException error) {
            System.err.println("[AlphaClient] Could not reset the client-side missed-swing delay: " + error.getMessage());
        }
    }

    /**
     * Recreates the 1.7.10 bow draw transform when enabled. The 1.7.10 renderer
     * scales the bow around its vertical midpoint; 1.8.9 scales around the
     * origin, which changes the old bow's pull-back appearance.
     *
     * The transformer replaces only ItemRenderer's bow-transform call. Other
     * item-use paths, blockhit, and fishing-rod rendering are left untouched.
     */
    public static void applyBowTransformations(
            Object renderer,
            float partialTicks,
            Object playerObject) {
        if (!(playerObject instanceof AbstractClientPlayer)) {
            return;
        }
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) playerObject;

        ItemStack activeItem = clientPlayer.getItemInUse();
        if (activeItem == null) {
            return;
        }

        // Common 1.7.10/1.8.9 bow pose and draw-progress calculation.
        GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.9F, 0.2F, 0.0F);

        float elapsedUse = (float) activeItem.getMaxItemUseDuration()
                - ((float) clientPlayer.getItemInUseCount() - partialTicks + 1.0F);
        float pullProgress = elapsedUse / 20.0F;
        pullProgress = (pullProgress * pullProgress + pullProgress * 2.0F) / 3.0F;
        if (pullProgress > 1.0F) {
            pullProgress = 1.0F;
        }

        if (pullProgress > 0.1F) {
            float bob = MathHelper.sin((elapsedUse - 0.1F) * 1.3F) * 0.01F * (pullProgress - 0.1F);
            GlStateManager.translate(0.0F, bob, 0.0F);
        }
        GlStateManager.translate(0.0F, 0.0F, pullProgress * 0.1F);

        if (isLegacyBowAnimationActive()) {
            // 1.7.10: apply the draw stretch around the bow model's midpoint.
            GlStateManager.rotate(-335.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-50.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.5F, 0.0F);
            GlStateManager.scale(1.0F, 1.0F, 1.0F + pullProgress * 0.2F);
            GlStateManager.translate(0.0F, -0.5F, 0.0F);
            GlStateManager.rotate(50.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(335.0F, 0.0F, 0.0F, 1.0F);
        } else {
            // Exact 1.8.9 vanilla scaling when the legacy option is switched off.
            GlStateManager.scale(1.0F, 1.0F, 1.0F + pullProgress * 0.2F);
        }
    }

    /**
     * Keeps the old 1.7.10 swing movement during bow use only while the legacy
     * bow option is enabled. Vanilla 1.8.9 passes zero for active item use.
     */
    public static float getLegacyBowSwingProgress(float swingProgress) {
        return getLegacyUseSwingProgress(swingProgress);
    }

    /** Existing bow-use swing gate; food/drink now has its own independent hook. */
    public static float getLegacyUseSwingProgress(float swingProgress) {
        if (!isLegacyBowAnimationActive()) return 0.0F;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return 0.0F;
        ItemStack active = mc.thePlayer.getItemInUse();
        if (active == null) active = mc.thePlayer.getHeldItem();
        if (active == null) return 0.0F;
        EnumAction action = active.getItemUseAction();
        return action == EnumAction.BOW ? swingProgress : 0.0F;
    }

    /**
     * Restores the legacy first-person swing while eating or drinking.
     * This is independent of the bow-animation setting.
     */
    public static float getLegacyFoodDrinkSwingProgress(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            return 0.0F;
        }

        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()
                || !LegacyPvpSettings.isFoodDrinkAnimationEnabled()) {
            return 0.0F;
        }

        ItemStack active = mc.thePlayer.getItemInUse();
        if (active == null) active = mc.thePlayer.getHeldItem();
        if (active == null) return 0.0F;

        EnumAction action = active.getItemUseAction();
        if (action != EnumAction.EAT && action != EnumAction.DRINK) return 0.0F;
        return mc.thePlayer.getSwingProgress(partialTicks);
    }

    private static boolean isLegacyBowAnimationActive() {
        if (!LegacyPvpSettings.isLegacyBowAnimationEnabled()) {
            return false;
        }

        AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
        return module != null && module.isImplemented() && module.isUnlocked() && module.isEnabled();
    }

    /**
     * Adds the classic 1.7.10-style first-person sword swing offsets before
     * vanilla's 1.8.9 item transform. The setting is independent of blockhit.
     */
    public static void applyLegacySwordSwing(Object renderer, float equipProgress, float swingProgress) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || mc.theWorld == null
                    || mc.currentScreen != null || swingProgress <= 0.0F
                    || !LegacyPvpSettings.isLegacySwordSwingEnabled()) {
                return;
            }

            AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
            if (module == null || !module.isImplemented() || !module.isUnlocked() || !module.isEnabled()) {
                return;
            }

            ItemStack held = mc.thePlayer.getHeldItem();
            if (held == null || !(held.getItem() instanceof ItemSword)) {
                return;
            }

            // Keep the legacy motion subtle: this hook runs before Minecraft's
            // vanilla first-person transform, so large offsets are amplified by
            // the remaining transform sequence and can push the sword off-screen.
            float swing = MathHelper.sqrt_float(swingProgress);
            float sinSwing = MathHelper.sin(swing * (float) Math.PI);
            float sinRoot = MathHelper.sin(swing * (float) Math.PI);
            GlStateManager.translate(-0.045F * sinRoot, 0.025F * sinSwing, 0.0F);
            GlStateManager.rotate(8.0F * sinRoot, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-5.0F * sinSwing, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-2.0F * sinRoot, 1.0F, 0.0F, 0.0F);
        } catch (RuntimeException error) {
            System.err.println("[AlphaClient] Could not apply Legacy Sword Swing: " + error.getMessage());
        }
    }

    /**
     * Supplies swing progress only for an actual sword blockhit:
     * the module is enabled, right-click is held, the player is using a sword,
     * and an attack swing is already in progress. Do not require a block target:
     * during PvP the crosshair is normally on an entity, not a world block.
     */
    public static float getBlockhitSwingProgress(float partialTicks) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || mc.theWorld == null
                    || mc.currentScreen != null
                    || (!Mouse.isButtonDown(1) && !mc.gameSettings.keyBindUseItem.isKeyDown())) {
                return 0.0F;
            }

            float swingProgress = mc.thePlayer.getSwingProgress(partialTicks);
            if (swingProgress <= 0.0F) return 0.0F;

            AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
            if (module == null || !module.isImplemented() || !module.isUnlocked()
                    || !module.isEnabled() || !LegacyPvpSettings.isBlockhitEnabled()) {
                return 0.0F;
            }

            // The render hook already runs inside the BLOCK item-use branch.
            // Prefer the active-use stack, but fall back to the held stack because
            // some 1.8.9 server/client states clear getItemInUse before this render.
            ItemStack usingItem = mc.thePlayer.getItemInUse();
            if (usingItem == null) {
                usingItem = mc.thePlayer.getHeldItem();
            }
            if (usingItem == null || !(usingItem.getItem() instanceof ItemSword)) {
                return 0.0F;
            }

            // A real blockhit is commonly aimed at an entity while attacking.
            // Requiring MovingObjectType.BLOCK here suppresses the legacy swing
            // in normal PvP combat, so the BLOCK render branch and sword gate
            // are the relevant checks instead.
            return swingProgress;
        } catch (RuntimeException error) {
            System.err.println("[AlphaClient] Could not read Legacy PvP blockhit state: " + error.getMessage());
            return 0.0F;
        }
    }
}
