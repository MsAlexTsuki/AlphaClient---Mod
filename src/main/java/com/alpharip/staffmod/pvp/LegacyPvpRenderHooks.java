package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
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

            MovingObjectPosition target = mc.objectMouseOver;
            if (target != null && target.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
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
            net.minecraft.client.renderer.ItemRenderer renderer,
            float partialTicks,
            AbstractClientPlayer clientPlayer) {
        if (clientPlayer == null) {
            return;
        }

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

        if (LegacyPvpSettings.isLegacyBowAnimationEnabled()) {
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
        return LegacyPvpSettings.isLegacyBowAnimationEnabled() ? swingProgress : 0.0F;
    }

    /**
     * Supplies swing progress only for an actual sword blockhit:
     * the module is enabled, right-click is held, the player is using a sword,
     * a block is targeted, and an attack swing is already in progress.
     * Holding right-click alone therefore never creates repeated blockhit swings.
     */
    public static float getBlockhitSwingProgress(float swingProgress) {
        if (swingProgress <= 0.0F) return 0.0F;

        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || mc.theWorld == null
                    || mc.currentScreen != null || !Mouse.isButtonDown(1)) {
                return 0.0F;
            }

            AlphaModule module = AlphaModuleRegistry.find("legacy-pvp");
            if (module == null || !module.isImplemented() || !module.isUnlocked()
                    || !module.isEnabled() || !LegacyPvpSettings.isBlockhitEnabled()) {
                return 0.0F;
            }

            ItemStack usingItem = mc.thePlayer.getItemInUse();
            if (usingItem == null || !(usingItem.getItem() instanceof ItemSword)) {
                return 0.0F;
            }

            MovingObjectPosition target = mc.objectMouseOver;
            if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return 0.0F;
            }

            return swingProgress;
        } catch (RuntimeException error) {
            System.err.println("[AlphaClient] Could not read Legacy PvP blockhit state: " + error.getMessage());
            return 0.0F;
        }
    }
}
