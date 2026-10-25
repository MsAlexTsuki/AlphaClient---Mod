package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class ModernGuiScreen extends AlphaGuiScreen {

    protected static final int GUI_WIDTH = 520;
    protected static final int GUI_HEIGHT = 300;
    protected static final int GUI_MARGIN = 12;


    /*
     * =========================
     * COLORS
     * =========================
     */

    protected static final int COLOR_WHITE =
            0xFFFFFFFF;

    protected static final int COLOR_GRAY =
            0xFFAAAAAA;

    protected static final int COLOR_DARK_GRAY =
            0xFF555555;

    protected static final int COLOR_GREEN =
            0xFF55FF55;

    protected static final int COLOR_RED =
            0xFFFF5555;


    /*
     * Transparante panels.
     *
     * Alpha:
     *
     * B8 = 72%
     * A0 = 63%
     * 8C = 55%
     */

    protected static final int COLOR_PANEL =
            0xA0101010;

    protected static final int COLOR_PANEL_LIGHT =
            0xA8151515;


    /*
     * Alpha branding
     */

    protected static final int COLOR_GOLD =
            0xFFF2EE4D;

    protected static final int COLOR_GOLD_OUTLINE =
            0xFF14181A;

    protected static final int COLOR_GOLD_SHADOW =
            0x99000000;


    /*
     * =========================
     * GUI SCALE
     * =========================
     */

    protected float getGuiScale() {

        float widthScale =
                this.width / 900.0F;

        float heightScale =
                this.height / 550.0F;

        float scale =
                Math.min(
                        widthScale,
                        heightScale
                );

        if (scale > 1.0F) {

            scale = 1.0F;
        }

        if (scale < 0.65F) {

            scale = 0.65F;
        }

        return scale;
    }


    protected void beginGuiScale() {

        float scale =
                getGuiScale();

        if (scale >= 0.999F) {

            return;
        }

        float centerX =
                this.width / 2.0F;

        float centerY =
                this.height / 2.0F;

        GL11.glPushMatrix();

        GL11.glTranslatef(
                centerX,
                centerY,
                0.0F
        );

        GL11.glScalef(
                scale,
                scale,
                1.0F
        );

        GL11.glTranslatef(
                -centerX,
                -centerY,
                0.0F
        );
    }


    protected void endGuiScale() {

        float scale =
                getGuiScale();

        if (scale >= 0.999F) {

            return;
        }

        GL11.glPopMatrix();
    }


    /*
     * =========================
     * MOUSE SCALE
     * =========================
     */

    protected int getScaledMouseX(
            int mouseX) {

        float scale =
                getGuiScale();

        if (scale >= 0.999F) {

            return mouseX;
        }

        float centerX =
                this.width / 2.0F;

        return Math.round(
                centerX
                        + (mouseX - centerX)
                        / scale
        );
    }


    protected int getScaledMouseY(
            int mouseY) {

        float scale =
                getGuiScale();

        if (scale >= 0.999F) {

            return mouseY;
        }

        float centerY =
                this.height / 2.0F;

        return Math.round(
                centerY
                        + (mouseY - centerY)
                        / scale
        );
    }


    /*
     * =========================
     * TRANSPARENT BACKGROUND
     * =========================
     */

    protected void drawModernBackground() {

        /*
         * De Minecraft wereld blijft
         * duidelijk zichtbaar.
         *
         * 0x35 = ongeveer 21% zwart.
         */

        drawRect(
                0,
                0,
                this.width,
                this.height,
                0x35000000
        );


        /*
         * Subtiele bovenbalk.
         *
         * Niet volledig zwart.
         */

        drawRect(
                0,
                0,
                this.width,
                42,
                0x700B0B0B
        );


        /*
         * Vanaf hier wordt de
         * centrale GUI-schaal gebruikt.
         */

        beginGuiScale();
    }


    /*
     * =========================
     * HEADER
     * =========================
     */

    protected void drawHeader(
            String title,
            String subtitle) {

        int centerX =
                this.width / 2;


        drawCenteredString(
                this.fontRendererObj,
                "ALPHA'S STAFFING MODS",
                centerX,
                13,
                COLOR_WHITE
        );


        drawCenteredString(
                this.fontRendererObj,
                title,
                centerX,
                55,
                COLOR_WHITE
        );


        if (subtitle != null
                && !subtitle.isEmpty()) {

            drawCenteredString(
                    this.fontRendererObj,
                    subtitle,
                    centerX,
                    71,
                    COLOR_GRAY
            );
        }
    }


    /*
     * =========================
     * ALPHA LOGO
     * =========================
     */

    protected void drawAlphaLogo(
            String text,
            int centerX,
            int y,
            int scale) {

        text = UiText.normalize(text);
        int textWidth =
                this.fontRendererObj
                        .getStringWidth(text);


        float x =
                centerX
                        - (textWidth * scale)
                        / 2.0F;


        GL11.glPushMatrix();


        GL11.glTranslatef(
                x,
                y,
                0
        );


        GL11.glScalef(
                scale,
                scale,
                1.0F
        );


        /*
         * Shadow
         */

        this.fontRendererObj.drawString(
                text,
                1,
                2,
                COLOR_GOLD_SHADOW
        );


        /*
         * Outline
         */

        for (int ox = -2;
             ox <= 2;
             ox++) {

            for (int oy = -2;
                 oy <= 2;
                 oy++) {

                if (ox == 0
                        && oy == 0) {

                    continue;
                }


                this.fontRendererObj.drawString(
                        text,
                        ox,
                        oy,
                        COLOR_GOLD_OUTLINE
                );
            }
        }


        /*
         * Gold text
         */

        this.fontRendererObj.drawString(
                text,
                0,
                0,
                COLOR_GOLD
        );


        GL11.glPopMatrix();
    }


    protected void drawModTitle(
            String modName,
            int centerX,
            int y) {

        drawAlphaLogo(
                "ALPHA'S " + modName,
                centerX,
                y,
                2
        );
    }


    /*
     * =========================
     * TRANSPARENT PANELS
     * =========================
     */

    protected void drawPanel(
            int left,
            int top,
            int right,
            int bottom) {

        /*
         * Transparante donkere body.
         */

        drawRect(
                left,
                top,
                right,
                bottom,
                COLOR_PANEL
        );


        /*
         * Subtiele bovenrand.
         */

        drawRect(
                left,
                top,
                right,
                top + 1,
                0xB0404040
        );


        /*
         * Subtiele onderrand.
         */

        drawRect(
                left,
                bottom - 1,
                right,
                bottom,
                0x80151515
        );


        /*
         * Linkerrand.
         */

        drawRect(
                left,
                top,
                left + 1,
                bottom,
                0x90303030
        );


        /*
         * Rechterrand.
         */

        drawRect(
                right - 1,
                top,
                right,
                bottom,
                0x90303030
        );
    }


    protected void drawCompactPanel(
            int top,
            int bottom) {

        drawPanel(
                getGuiLeft(),
                top,
                getGuiRight(),
                bottom
        );
    }


    /*
     * =========================
     * ROW LABEL
     * =========================
     */

    protected void drawRowLabel(
            String text,
            int x,
            int y) {

        this.fontRendererObj.drawStringWithShadow(
                text,
                x,
                y,
                COLOR_WHITE
        );
    }


    /*
     * =========================
     * TITLE
     * =========================
     */

    protected void drawTitle(
            String title,
            String subtitle) {

        drawHeader(
                title,
                subtitle
        );
    }


    /*
     * =========================
     * GUI DIMENSIONS
     * =========================
     */

    protected int getResponsiveGuiWidth() {

        return GUI_WIDTH;
    }


    protected int getResponsiveGuiHeight() {

        return GUI_HEIGHT;
    }


    protected int getResponsivePanelWidth(
            int preferredWidth) {

        return preferredWidth;
    }


    protected int getGuiLeft() {

        int width =
                getResponsiveGuiWidth();

        return this.width / 2
                - width / 2;
    }


    protected int getGuiRight() {

        return getGuiLeft()
                + getResponsiveGuiWidth();
    }


    protected int getGuiCenter() {

        return this.width / 2;
    }


    protected int getGuiTop() {

        int height =
                getResponsiveGuiHeight();

        return this.height / 2
                - height / 2;
    }


    protected int getGuiBottom() {

        return getGuiTop()
                + getResponsiveGuiHeight();
    }


    protected boolean isSmallScreen() {

        return getGuiScale() < 0.90F;
    }


    protected boolean isVerySmallScreen() {

        return getGuiScale() < 0.75F;
    }


    /*
     * =========================
     * CLAMP
     * =========================
     */

    protected int clampX(
            int x,
            int elementWidth) {

        int max =
                Math.max(
                        0,
                        this.width
                                - Math.max(
                                0,
                                elementWidth
                        )
                );


        return Math.max(
                0,
                Math.min(
                        x,
                        max
                )
        );
    }


    protected int clampY(
            int y,
            int elementHeight) {

        int max =
                Math.max(
                        0,
                        this.height
                                - Math.max(
                                0,
                                elementHeight
                        )
                );


        return Math.max(
                0,
                Math.min(
                        y,
                        max
                )
        );
    }


    /*
     * =========================
     * MOUSE CLICK
     * =========================
     */

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton)
            throws java.io.IOException {

        super.mouseClicked(
                getScaledMouseX(mouseX),
                getScaledMouseY(mouseY),
                mouseButton
        );
    }


    /*
     * =========================
     * DRAW SCREEN
     * =========================
     */

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        super.drawScreen(
                getScaledMouseX(mouseX),
                getScaledMouseY(mouseY),
                partialTicks
        );


        /*
         * Sluit de centrale
         * GUI-scale af.
         */

        endGuiScale();
    }


    /*
     * =========================
     * GAME
     * =========================
     */

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}