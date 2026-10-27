// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class MainMenuGui extends ModernGuiScreen {

    private static final int BUTTON_STAFF_MODS = 1;
    private static final int BUTTON_MODS = 2;
    private static final int BUTTON_CLOSE = 3;

    private static final ResourceLocation ALPHA_LOGO =
            new ResourceLocation(
                    "alphastaffmod",
                    "textures/gui/alpha_logo.png"
            );

    /*
     * =========================
     * LOGO / MENU ANIMATION
     * =========================
     */

    private static final int LOGO_WIDTH = 80;
    private static final int LOGO_HEIGHT = 80;

    private static final int LOGO_TOP_Y = 10;

    /*
     * Hoe lang het duurt voordat het logo
     * vanuit het midden naar boven beweegt.
     */
    private static final long LOGO_ANIMATION_MS = 500L;

    /*
     * Menu blijft verborgen totdat het logo
     * klaar is met bewegen.
     */
    private boolean menuOpen = false;
    private boolean logoAnimating = false;
    private long logoAnimationStart = 0L;


    /*
     * =========================
     * INIT
     * =========================
     */

    @Override
    public void initGui() {

        this.buttonList.clear();

        int centerX =
                this.width / 2;

        /*
         * Buttons staan al op hun uiteindelijke
         * positie. Ze worden pas zichtbaar/
         * klikbaar nadat de logo-animatie klaar is.
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_STAFF_MODS,
                        centerX - 120,
                        165,
                        240,
                        24,
                        "STAFF MODS"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_MODS,
                        centerX - 120,
                        198,
                        240,
                        24,
                        "MODS"
                )
        );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_CLOSE,
                        centerX - 120,
                        231,
                        240,
                        24,
                        "CLOSE"
                )
        );
    }


    /*
     * =========================
     * BUTTON ACTIONS
     * =========================
     */

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        if (button == null) {
            return;
        }

        if (!menuOpen || logoAnimating) {
            return;
        }

        if (button.id == BUTTON_STAFF_MODS) {

            this.mc.displayGuiScreen(
                    new StaffModsMenuGui(this)
            );

            return;
        }

        if (button.id == BUTTON_MODS) {

            this.mc.displayGuiScreen(
                    new ModsGui(this)
            );

            return;
        }

        if (button.id == BUTTON_CLOSE) {

            this.mc.displayGuiScreen(
                    null
            );
        }
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
            throws IOException {

        int scaledMouseX =
                getScaledMouseX(mouseX);

        int scaledMouseY =
                getScaledMouseY(mouseY);

        /*
         * =========================
         * LOGO CLICK
         * =========================
         *
         * Wanneer het menu nog dicht is,
         * staat het logo exact in het midden
         * van het scherm.
         */

        if (!menuOpen
                && !logoAnimating
                && mouseButton == 0) {

            int centerX =
                    this.width / 2;

            int centerY =
                    this.height / 2;

            int logoLeft =
                    centerX - LOGO_WIDTH / 2;

            int logoRight =
                    centerX + LOGO_WIDTH / 2;

            int logoTop =
                    centerY - LOGO_HEIGHT / 2;

            int logoBottom =
                    centerY + LOGO_HEIGHT / 2;

            if (scaledMouseX >= logoLeft
                    && scaledMouseX <= logoRight
                    && scaledMouseY >= logoTop
                    && scaledMouseY <= logoBottom) {

                logoAnimating = true;
                logoAnimationStart =
                        System.currentTimeMillis();

                return;
            }
        }

        /*
         * Klikken buiten het logo doet niets
         * zolang het hoofdmenu nog dicht is.
         */
        if (!menuOpen) {
            return;
        }

        /*
         * Tijdens de animatie zijn de buttons
         * bewust niet klikbaar.
         */
        if (logoAnimating) {
            return;
        }

        /*
         * ModernGuiScreen verzorgt zelf de
         * omzetting van de muiscoördinaten.
         *
         * Daarom geven we hier de ORIGINELE
         * muiscoördinaten door. Een tweede
         * scaling zou de klikzone verschuiven.
         */
        super.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );
    }


    /*
     * =========================
     * DRAW
     * =========================
     */

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        drawModernBackground();

        int centerX =
                this.width / 2;

        int centerY =
                this.height / 2;


        /*
         * =========================
         * ANIMATION STATE
         * =========================
         */

        /*
         * Als het menu al geopend is, moet het logo
         * blijvend op zijn eindpositie boven het menu
         * blijven staan.
         */
        float animationProgress =
                menuOpen ? 1.0F : 0.0F;

        if (logoAnimating) {

            long elapsed =
                    System.currentTimeMillis()
                            - logoAnimationStart;

            float rawProgress =
                    Math.min(
                            1.0F,
                            (float) elapsed
                                    / (float) LOGO_ANIMATION_MS
                    );

            /*
             * Smooth ease-out:
             *
             * Eerst beweegt het logo snel,
             * daarna remt het netjes af.
             */
            float inverse =
                    1.0F - rawProgress;

            animationProgress =
                    1.0F
                            - inverse * inverse * inverse;

            if (elapsed >= LOGO_ANIMATION_MS) {

                animationProgress = 1.0F;
                logoAnimating = false;
                menuOpen = true;
            }
        }


        /*
         * =========================
         * LOGO POSITION
         * =========================
         */

        int centerLogoY =
                centerY - LOGO_HEIGHT / 2;

        int logoY =
                centerLogoY;

        if (logoAnimating || menuOpen) {

            logoY =
                    (int) (
                            centerLogoY
                                    + (LOGO_TOP_Y
                                    - centerLogoY)
                                    * animationProgress
                    );
        }


        /*
         * =========================
         * ALPHA LOGO
         * =========================
         */

        drawAlphaLogoTexture(
                centerX,
                logoY,
                LOGO_WIDTH,
                LOGO_HEIGHT
        );


        /*
         * =========================
         * MENU
         * =========================
         */

        if (menuOpen) {

            /*
             * =========================
             * SUBTITLE
             * =========================
             */

            drawCenteredString(
                    this.fontRendererObj,
                    "Select a mod category",
                    centerX,
                    100,
                    COLOR_GOLD
            );


            /*
             * =========================
             * INFORMATION PANEL
             * =========================
             */

            int panelWidth =
                    300;

            int panelLeft =
                    centerX
                            - panelWidth / 2;

            int panelRight =
                    centerX
                            + panelWidth / 2;

            drawPanel(
                    panelLeft,
                    125,
                    panelRight,
                    270
            );


            /*
             * =========================
             * TITLE
             * =========================
             */

            drawCenteredString(
                    this.fontRendererObj,
                    "STAFF MODS",
                    centerX,
                    137,
                    COLOR_GREEN
            );


            /*
             * =========================
             * BUTTONS
             * =========================
             */

            super.drawScreen(
                    mouseX,
                    mouseY,
                    partialTicks
            );
        }
    }


    /*
     * =========================
     * ALPHA LOGO TEXTURE
     * =========================
     */

    private void drawAlphaLogoTexture(
            int centerX,
            int y,
            int width,
            int height) {

        Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(ALPHA_LOGO);

        GlStateManager.pushMatrix();

        GlStateManager.color(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        drawModalRectWithCustomSizedTexture(
                centerX - width / 2,
                y,
                0,
                0,
                width,
                height,
                width,
                height
        );

        GlStateManager.popMatrix();
    }


    /*
     * =========================
     * ESC
     * =========================
     */

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode)
            throws IOException {

        if (keyCode == 1) {

            /*
             * ESC sluit de volledige MainMenuGui
             * direct. Het menu gaat dus niet eerst
             * terug naar het losse logo.
             */
            this.mc.displayGuiScreen(
                    null
            );

            return;
        }

        super.keyTyped(
                typedChar,
                keyCode
        );
    }


    /*
     * =========================
     * GUI BEHAVIOR
     * =========================
     */

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}
