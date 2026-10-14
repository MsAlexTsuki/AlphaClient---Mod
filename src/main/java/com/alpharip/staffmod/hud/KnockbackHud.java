package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.util.KnockbackTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class KnockbackHud {

    private static final int HUD_WIDTH = 175;
    private static final int HUD_HEIGHT = 70;

    /*
     * HUD blijft 5 seconden zichtbaar
     * nadat je niet meer naar een speler kijkt.
     */
    private static final long HIDE_DELAY_MS = 5000L;

    private long lastPlayerTargetTime = 0L;

    @SubscribeEvent
    public void onRender(
            RenderGameOverlayEvent.Post event) {

        if (event.type
                != RenderGameOverlayEvent.ElementType.ALL) {

            return;
        }

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.thePlayer == null) {

            return;
        }

        if (!KnockbackTracker.isEnabled()) {
            return;
        }

        if (mc.currentScreen != null) {
            return;
        }

        if (mc.gameSettings.hideGUI) {
            return;
        }

        boolean lookingAtPlayer =
                isLookingAtPlayer(mc);

        if (lookingAtPlayer) {

            lastPlayerTargetTime =
                    System.currentTimeMillis();
        }

        if (lastPlayerTargetTime == 0L) {
            return;
        }

        long elapsed =
                System.currentTimeMillis()
                        - lastPlayerTargetTime;

        if (elapsed > HIDE_DELAY_MS) {
            return;
        }

        renderHud(mc);
    }

    private boolean isLookingAtPlayer(
            Minecraft mc) {

        if (mc.objectMouseOver == null) {
            return false;
        }

        if (mc.objectMouseOver.entityHit == null) {
            return false;
        }

        if (!(mc.objectMouseOver.entityHit
                instanceof EntityPlayer)) {

            return false;
        }

        EntityPlayer target =
                (EntityPlayer)
                        mc.objectMouseOver.entityHit;

        return target != mc.thePlayer;
    }

    private void renderHud(
            Minecraft mc) {

        FontRenderer font =
                mc.fontRendererObj;

        float scale =
                KnockbackTracker.getVisualScale();

        int x =
                KnockbackTracker.getHudX();

        int y =
                KnockbackTracker.getHudY();

        String target =
                KnockbackTracker.getTargetName();

        double expected =
                KnockbackTracker
                        .getExpectedVelocity();

        double actual =
                KnockbackTracker
                        .getActualVelocity();

        double ratio =
                KnockbackTracker
                        .getRatio();

        int vl =
                KnockbackTracker
                        .getViolationLevel();

        /*
         * HUD kleuren.
         *
         * 0x8C = ongeveer 45% transparant.
         */
        int background =
                0x8C101010;

        int border =
                0x30FFFFFF;

        int borderBottom =
                0x20FFFFFF;

        int white =
                0xFFFFFFFF;

        int gray =
                0xFFAAAAAA;

        int ratioColor;

        if (ratio >= 0.75D) {

            ratioColor =
                    0xFF55FF55;

        } else if (ratio >= 0.40D) {

            ratioColor =
                    0xFFFFAA00;

        } else {

            ratioColor =
                    0xFFFF5555;
        }

        GlStateManager.pushMatrix();

        GlStateManager.translate(
                x,
                y,
                0.0F
        );

        GlStateManager.scale(
                scale,
                scale,
                scale
        );

        /*
         * Transparante achtergrond.
         */
        drawRect(
                0,
                0,
                HUD_WIDTH,
                HUD_HEIGHT,
                background
        );

        /*
         * Geen groene bovenrand.
         */

        /*
         * Zeer subtiele witte randen.
         */
        drawRect(
                0,
                0,
                1,
                HUD_HEIGHT,
                border
        );

        drawRect(
                HUD_WIDTH - 1,
                0,
                HUD_WIDTH,
                HUD_HEIGHT,
                border
        );

        drawRect(
                0,
                HUD_HEIGHT - 1,
                HUD_WIDTH,
                HUD_HEIGHT,
                borderBottom
        );

        /*
         * Tekst.
         */
        font.drawStringWithShadow(
                "KNOCKBACK",
                8,
                6,
                white
        );

        font.drawStringWithShadow(
                "Target:",
                8,
                19,
                gray
        );

        font.drawStringWithShadow(
                target,
                55,
                19,
                white
        );

        font.drawStringWithShadow(
                "Expected:",
                8,
                32,
                gray
        );

        font.drawStringWithShadow(
                String.format(
                        "%.3f",
                        expected
                ),
                65,
                32,
                white
        );

        font.drawStringWithShadow(
                "Actual:",
                8,
                45,
                gray
        );

        font.drawStringWithShadow(
                String.format(
                        "%.3f",
                        actual
                ),
                65,
                45,
                white
        );

        font.drawStringWithShadow(
                "Ratio:",
                8,
                58,
                gray
        );

        font.drawStringWithShadow(
                String.format(
                        "%.0f%%",
                        ratio * 100.0D
                ),
                48,
                58,
                ratioColor
        );

        font.drawStringWithShadow(
                "VL:",
                115,
                58,
                gray
        );

        font.drawStringWithShadow(
                String.valueOf(vl),
                135,
                58,
                vl > 0
                        ? 0xFFFF5555
                        : 0xFF55FF55
        );

        GlStateManager.popMatrix();
    }

    private static void drawRect(
            int left,
            int top,
            int right,
            int bottom,
            int color) {

        net.minecraft.client.gui.Gui.drawRect(
                left,
                top,
                right,
                bottom,
                color
        );
    }
}