package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.NoFallMod;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class NoFallSettingsGui extends ModernGuiScreen {

    private static final int BUTTON_TOGGLE = 1;
    private static final int BUTTON_BACK = 2;

    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 185;

    private static final int BUTTON_WIDTH = 240;
    private static final int BUTTON_HEIGHT = 30;

    private final GuiScreen parentScreen;

    public NoFallSettingsGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int panelLeft = this.width / 2 - PANEL_WIDTH / 2;
        int panelTop = 85;

        /*
         * Toggle button
         *
         * Kept well below the description so
         * nothing overlaps.
         */
        this.buttonList.add(
                new ModernButton(
                        BUTTON_TOGGLE,
                        this.width / 2 - BUTTON_WIDTH / 2,
                        panelTop + 105,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        getToggleText()
                )
        );

        /*
         * Back button
         *
         * Same general placement style as the
         * other Alpha Client GUIs.
         */
        this.buttonList.add(
                new ModernButton(
                        BUTTON_BACK,
                        this.width / 2 - BUTTON_WIDTH / 2,
                        panelTop + PANEL_HEIGHT - 25,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        "BACK"
                )
        );
    }

    private String getToggleText() {
        return NoFallMod.getInstance().isEnabled()
                ? "NOFALL: ON"
                : "NOFALL: OFF";
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        if (button.id == BUTTON_TOGGLE) {
            NoFallMod.getInstance().toggle();

            button.displayString = getToggleText();
            return;
        }

        if (button.id == BUTTON_BACK) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        drawModernBackground();

        /*
         * Main Alpha Client title
         */
        drawModTitle(
                "NOFALL",
                this.width / 2,
                18
        );

        /*
         * Small subtitle underneath the title.
         */
        drawCenteredString(
                this.fontRendererObj,
                "Fall damage protection",
                this.width / 2,
                48,
                COLOR_GOLD
        );

        /*
         * Main transparent panel.
         *
         * This follows the same visual approach
         * as the other Alpha Client GUIs:
         * transparent dark background + subtle
         * white outline.
         */
        int panelLeft = this.width / 2 - PANEL_WIDTH / 2;
        int panelTop = 70;
        int panelRight = panelLeft + PANEL_WIDTH;
        int panelBottom = panelTop + PANEL_HEIGHT;

        drawRect(
                panelLeft,
                panelTop,
                panelRight,
                panelBottom,
                0x38101010
        );

        /*
         * Subtle panel outline.
         */
        drawRect(
                panelLeft,
                panelTop,
                panelRight,
                panelTop + 1,
                0x38E8E8E8
        );

        drawRect(
                panelLeft,
                panelBottom - 1,
                panelRight,
                panelBottom,
                0x38E8E8E8
        );

        drawRect(
                panelLeft,
                panelTop,
                panelLeft + 1,
                panelBottom,
                0x38E8E8E8
        );

        drawRect(
                panelRight - 1,
                panelTop,
                panelRight,
                panelBottom,
                0x38E8E8E8
        );

        /*
         * Panel heading.
         */
        drawCenteredString(
                this.fontRendererObj,
                "NoFall",
                this.width / 2,
                panelTop + 20,
                COLOR_WHITE
        );

        /*
         * Description.
         *
         * Kept on its own line and with enough
         * vertical space before the button.
         */
        drawCenteredString(
                this.fontRendererObj,
                "Prevents normal fall damage while enabled.",
                this.width / 2,
                panelTop + 48,
                COLOR_WHITE
        );

        /*
         * Current status.
         */
        boolean enabled =
                NoFallMod.getInstance().isEnabled();

        drawCenteredString(
                this.fontRendererObj,
                "Status: " + (enabled ? "ON" : "OFF"),
                this.width / 2,
                panelTop + 70,
                enabled
                        ? COLOR_GOLD
                        : COLOR_WHITE
        );

        /*
         * Draw buttons last so they are always
         * visible above the panel.
         */
        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode
    ) throws IOException {

        if (keyCode == 1) {
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }

        super.keyTyped(
                typedChar,
                keyCode
        );
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}