package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.profile.ProfileManager;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class BhopHud {

    private static boolean enabled = true;

    private static boolean editorPreviewMode = false;

    /** Bhop HUD visibility follows the same staff permission as the Bhop module. */
    private static boolean hasBhopPermission() {
        AlphaModule module = AlphaModuleRegistry.find("staffmods");
        return module != null && module.isUnlocked();
    }


    /*
     * =========================================================
     * POSITION
     * =========================================================
     */

    private static int hudX = 5;
    private static int hudY = 100;


    /*
     * =========================================================
     * SCALE
     * =========================================================
     */

    private static int hudScale = 50;


    /*
     * =========================================================
     * HUD SIZE
     * =========================================================
     */

    private static final int HUD_HEIGHT = 20;

    private static final int HUD_PADDING_LEFT = 8;
    private static final int HUD_PADDING_RIGHT = 8;



    /*
     * =========================================================
     * TOGGLE
     * =========================================================
     */

    public static void toggle() {

        enabled =
                !enabled;
    }


    public static boolean isEnabled() {

        return enabled;
    }


    public static void setEnabled(
            boolean value) {

        enabled =
                value;
    }


    /*
     * =========================================================
     * EDITOR PREVIEW MODE
     * =========================================================
     */

    public static void setEditorPreviewMode(
            boolean value) {

        editorPreviewMode =
                value;
    }


    public static boolean isEditorPreviewMode() {

        return editorPreviewMode;
    }


    /*
     * =========================================================
     * POSITION
     * =========================================================
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

        hudX =
                x;

        hudY =
                y;

        HudPositionConfig.setBhopPosition(
                x,
                y
        );
    }


    public static void resetPosition() {

        hudX =
                5;

        hudY =
                100;

        HudPositionConfig.resetBhopPosition();
    }


    /*
     * =========================================================
     * LOAD POSITION
     * =========================================================
     */

    public static void loadHudPosition() {

        HudPositionConfig.load();

        hudX =
                HudPositionConfig.getBhopX();

        hudY =
                HudPositionConfig.getBhopY();

        hudScale =
                HudPositionConfig.getBhopScale();
    }


    /*
     * =========================================================
     * SCALE
     * =========================================================
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

        hudScale =
                scale;

        HudPositionConfig.setBhopScale(
                scale
        );
    }


    public static float getVisualScale() {

        return 0.50F
                + (
                hudScale / 100.0F
        );
    }


    /*
     * =========================================================
     * BHOP NAME
     * =========================================================
     */

    public static String getDisplayedBhopName() {

        String bhopName =
                BhopMode.getBhopName();

        if (bhopName == null
                || bhopName.trim().isEmpty()
                || bhopName.equalsIgnoreCase("BHOP")) {

            return "Alpha's Bhop";
        }

        return bhopName;
    }


    /*
     * =========================================================
     * COMPATIBILITY
     * =========================================================
     */

    public static String getDisplayedProfileName() {

        return getDisplayedBhopName();
    }


    /*
     * =========================================================
     * SPEED NAME
     * =========================================================
     */

    public static String getDisplayedSpeedName() {

        return BhopMode.getSpeedLevelName();
    }


    /*
     * =========================================================
     * HUD WIDTH
     * =========================================================
     */

    public static int getHudWidth() {

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.fontRendererObj == null) {

            return 180;
        }

        FontRenderer font =
                mc.fontRendererObj;

        String bhopName =
                getDisplayedBhopName();

        int bhopNameWidth =
                font.getStringWidth(
                        bhopName
                );

        return HUD_PADDING_LEFT
                + bhopNameWidth
                + HUD_PADDING_RIGHT;
    }


    /*
     * =========================================================
     * RENDER
     * =========================================================
     */

    @SubscribeEvent
    public void onRender(
            RenderGameOverlayEvent.Post event) {

        if (event.type
                != RenderGameOverlayEvent.ElementType.ALL) {

            return;
        }

        if (!hasBhopPermission()) {
            return;
        }

        if (!enabled) {
            return;
        }

        // The BHOP HUD is tied to the BHOP module state.
        // Keep editor preview independent so users can still position the HUD while BHOP is off.
        if (!BhopMode.isEnabled() && !editorPreviewMode) {
            return;
        }

        if (editorPreviewMode) {
            return;
        }

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.thePlayer == null) {

            return;
        }

        if (mc.gameSettings.hideGUI) {
            return;
        }

        renderHud(mc);
    }


    /*
     * =========================================================
     * DRAW HUD
     * =========================================================
     */

    /** Draws the live BHOP HUD style inside the HUD editor preview. */
    public static void renderEditorPreview() {
        if (!hasBhopPermission() || !enabled) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null) {
            return;
        }
        renderHud(mc);
    }

    private static void renderHud(
            Minecraft mc) {

        FontRenderer font =
                mc.fontRendererObj;

        float scale =
                getVisualScale();


        String bhopName =
                getDisplayedBhopName();

        String speedName =
                getDisplayedSpeedName();


        int hudWidth =
                getHudWidth();


        /*
         * =====================================================
         * SCALE / POSITION
         * =====================================================
         */

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
         * AlphaClient compact HUD panel.
         */
        drawRect(0, 0, hudWidth, HUD_HEIGHT, 0xD9101218);
        drawRect(0, 0, hudWidth, 2, 0xFFFFD866);
        drawRect(0, 2, 2, HUD_HEIGHT, 0xFFFFD866);
        drawRect(2, HUD_HEIGHT - 1, hudWidth, HUD_HEIGHT, 0x553F444E);

        /*
         * Profile label keeps its configured color.
         */
        int bhopNameColor =
                parseColor(
                        ProfileManager
                                .getInstance()
                                .getColor("BHOP_PROFILE_NAME")
                );
        font.drawStringWithShadow(
                bhopName,
                HUD_PADDING_LEFT,
                6,
                bhopNameColor
        );

        GlStateManager.popMatrix();
    }


    /*
     * =========================================================
     * COLOR
     * =========================================================
     */

    private static int parseColor(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return 0xFFF2EE4D;
        }

        try {

            String clean =
                    value.trim();

            if (clean.startsWith("#")) {
                clean =
                        clean.substring(1);
            }

            long parsed =
                    Long.parseLong(
                            clean,
                            16
                    );

            if (clean.length() <= 6) {

                parsed |=
                        0xFF000000L;
            }

            return (int) parsed;

        } catch (Exception ignored) {

            return 0xFFF2EE4D;
        }
    }


    /*
     * =========================================================
     * RECTANGLE
     * =========================================================
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