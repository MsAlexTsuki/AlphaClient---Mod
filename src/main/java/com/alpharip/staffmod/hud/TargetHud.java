package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.util.CPSTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TargetHud {

    private static boolean enabled = true;

    private static int hudX = 5;
    private static int hudY = 5;

    /*
     * 0 - 100
     *
     * 0   = 50%
     * 50  = 100%
     * 100 = 150%
     */
    private static int hudScale = 50;

    private static final int HUD_WIDTH = 145;
    private static final int HUD_HEIGHT = 48;


    /*
     * =========================
     * TOGGLE
     * =========================
     */

    public static void toggle() {

        enabled = !enabled;
    }

    public static boolean isEnabled() {

        return enabled;
    }

    public static void setEnabled(
            boolean value) {

        enabled = value;
    }


    /*
     * =========================
     * POSITION
     * =========================
     */

    public static int getHudX() {

        return hudX;
    }

    public static int getHudY() {

        return hudY;
    }

    public static void setHudPosition(
            int x,
            int y) {

        hudX = x;
        hudY = y;

        HudPositionConfig.setCpsPosition(
                x,
                y
        );
    }

    public static void resetPosition() {

        hudX = 5;
        hudY = 5;

        HudPositionConfig.resetCpsPosition();
    }

    public static void loadHudPosition() {

        HudPositionConfig.load();

        hudX =
                HudPositionConfig.getCpsX();

        hudY =
                HudPositionConfig.getCpsY();

        hudScale =
                HudPositionConfig.getCpsScale();
    }


    /*
     * =========================
     * SCALE
     * =========================
     */

    public static int getHudScale() {

        return hudScale;
    }

    public static void setHudScale(
            int scale) {

        if (scale < 0) {

            scale = 0;
        }

        if (scale > 100) {

            scale = 100;
        }

        hudScale = scale;

        HudPositionConfig.setCpsScale(
                scale
        );
    }

    public static float getVisualScale() {

        return 0.50F
                + (hudScale / 100.0F);
    }


    /*
     * =========================
     * RENDER
     * =========================
     */

    @SubscribeEvent
    public void onRender(
            RenderGameOverlayEvent.Post event) {

        if (event.type
                != RenderGameOverlayEvent.ElementType.ALL) {

            return;
        }

        if (!enabled) {

            return;
        }

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.thePlayer == null) {

            return;
        }

        if (mc.theWorld == null) {

            return;
        }

        if (mc.gameSettings.hideGUI) {

            return;
        }

        /*
         * Alleen de speler waar de
         * crosshair daadwerkelijk op staat.
         */
        EntityPlayer target =
                getTargetPlayer(mc);

        /*
         * Geen speler in beeld =
         * volledige HUD verbergen.
         */
        if (target == null) {

            return;
        }

        renderHud(
                mc,
                target
        );
    }


    /*
     * =========================
     * TARGET
     * =========================
     *
     * Alleen de entity onder de
     * crosshair wordt gebruikt.
     *
     * Dus NIET meer de dichtstbijzijnde
     * speler uit playerEntities.
     */

    private EntityPlayer getTargetPlayer(
            Minecraft mc) {

        if (mc.objectMouseOver == null) {

            return null;
        }

        if (mc.objectMouseOver.entityHit
                == null) {

            return null;
        }

        if (!(mc.objectMouseOver.entityHit
                instanceof EntityPlayer)) {

            return null;
        }

        EntityPlayer target =
                (EntityPlayer)
                        mc.objectMouseOver.entityHit;

        if (target == mc.thePlayer) {

            return null;
        }

        if (target.isDead) {

            return null;
        }

        return target;
    }


    /*
     * =========================
     * HUD
     * =========================
     */

    private void renderHud(
            Minecraft mc,
            EntityPlayer target) {

        FontRenderer font =
                mc.fontRendererObj;

        float scale =
                getVisualScale();

        int cps =
                CPSTracker.getCPS(
                        target.getEntityId()
                );

        double distance =
                mc.thePlayer.getDistanceToEntity(
                        target
                );

        GlStateManager.pushMatrix();

        GlStateManager.translate(
                hudX,
                hudY,
                0.0F
        );

        GlStateManager.scale(
                scale,
                scale,
                scale
        );

        /*
         * Background
         */
        drawRect(
                0,
                0,
                HUD_WIDTH,
                HUD_HEIGHT,
                0xCC101010
        );

        /*
         * Green top border
         */
        drawRect(
                0,
                0,
                HUD_WIDTH,
                2,
                0xFF55FF55
        );

        /*
         * Green left border
         */
        drawRect(
                0,
                0,
                2,
                HUD_HEIGHT,
                0xFF55FF55
        );

        /*
         * Player name
         */
        font.drawStringWithShadow(
                target.getName(),
                8,
                7,
                0xFFFFFFFF
        );

        /*
         * Distance
         */
        font.drawStringWithShadow(
                String.format(
                        "Distance: %.1fm",
                        distance
                ),
                8,
                20,
                0xFFAAAAAA
        );

        /*
         * CPS color
         */
        int cpsColor;

        if (cps >= 20) {

            cpsColor =
                    0xFFFF5555;

        } else if (cps >= 12) {

            cpsColor =
                    0xFFFFAA00;

        } else {

            cpsColor =
                    0xFF55FF55;
        }

        /*
         * CPS
         */
        font.drawStringWithShadow(
                "CPS: " + cps,
                8,
                33,
                cpsColor
        );

        GlStateManager.popMatrix();
    }


    /*
     * =========================
     * RECT
     * =========================
     */

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