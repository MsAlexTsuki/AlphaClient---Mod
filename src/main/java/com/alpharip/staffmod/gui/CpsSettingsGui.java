package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.TargetHud;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class CpsSettingsGui extends ModernGuiScreen {

    private static final int BUTTON_TOGGLE = 1;
    private static final int BUTTON_EDITOR = 2;
    private static final int BUTTON_BACK = 3;

    private static final int ACTION_BUTTON_WIDTH = 174;
    private static final int ACTION_BUTTON_HEIGHT = 30;

    private final GuiScreen parentScreen;

    public CpsSettingsGui(
            GuiScreen parentScreen) {

        this.parentScreen =
                parentScreen;
    }

    @Override
    public void initGui() {

        this.buttonList.clear();

        int centerX =
                this.width / 2;

        int panelWidth =
                Math.min(
                        520,
                        this.width - 30
                );

        int panelLeft =
                centerX
                        - panelWidth / 2;

        int panelRight =
                panelLeft
                        + panelWidth;

        /*
         * =========================
         * BUTTON POSITION
         * =========================
         */

        int buttonX =
                panelRight
                        - ACTION_BUTTON_WIDTH
                        - 14;

        /*
         * =========================
         * CPS
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_TOGGLE,
                        buttonX,
                        165,
                        ACTION_BUTTON_WIDTH,
                        ACTION_BUTTON_HEIGHT,
                        "CPS: " + getToggleText()
                )
        );

        /*
         * =========================
         * HUD EDITOR
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_EDITOR,
                        buttonX,
                        201,
                        ACTION_BUTTON_WIDTH,
                        ACTION_BUTTON_HEIGHT,
                        "HUD EDITOR"
                )
        );

        /*
         * =========================
         * BACK
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_BACK,
                        this.width / 2 - 55,
                        this.height - 35,
                        110,
                        20,
                        "BACK"
                )
        );
    }

    private String getToggleText() {

        return TargetHud.isEnabled()
                ? "ON"
                : "OFF";
    }

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        if (button == null) {
            return;
        }

        if (button.id ==
                BUTTON_TOGGLE) {

            TargetHud.toggle();

            button.displayString =
                    "CPS: "
                            + getToggleText();

            return;
        }

        if (button.id ==
                BUTTON_EDITOR) {

            this.mc.displayGuiScreen(
                    new HudEditorGui(this)
            );

            return;
        }

        if (button.id ==
                BUTTON_BACK) {

            this.mc.displayGuiScreen(
                    parentScreen
            );
        }
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        drawModernBackground();

        int centerX =
                this.width / 2;

        /*
         * =========================
         * PANEL
         * =========================
         */

        int panelWidth =
                Math.min(
                        520,
                        this.width - 30
                );

        int panelLeft =
                centerX
                        - panelWidth / 2;

        int panelRight =
                panelLeft
                        + panelWidth;

        int panelTop =
                isSmallScreen()
                        ? 90
                        : 95;

        int panelBottom =
                isSmallScreen()
                        ? 270
                        : 255;

        /*
         * =========================
         * TITLE
         * =========================
         */

        drawModTitle(
                "CPS INDICATOR",
                centerX,
                isSmallScreen()
                        ? 27
                        : 30
        );

        /*
         * =========================
         * TRANSPARENT PANEL
         * =========================
         */

        drawRect(
                panelLeft,
                panelTop,
                panelRight,
                panelBottom,
                0x50101010
        );

        int contentX =
                panelLeft + 18;

        /*
         * =========================
         * CPS INDICATOR
         * =========================
         */

        drawRowLabel(
                "CPS INDICATOR",
                contentX,
                panelTop + 18
        );

        this.fontRendererObj.drawStringWithShadow(
                "Shows the CPS of the player you are looking at.",
                contentX,
                panelTop + 40,
                COLOR_GRAY
        );

        /*
         * =========================
         * STATUS
         * =========================
         */

        drawRowLabel(
                "STATUS",
                contentX,
                panelTop + 78
        );

        this.fontRendererObj.drawStringWithShadow(
                TargetHud.isEnabled()
                        ? "ENABLED"
                        : "DISABLED",
                contentX + 65,
                panelTop + 78,
                TargetHud.isEnabled()
                        ? COLOR_GREEN
                        : COLOR_RED
        );

        /*
         * =========================
         * HUD EDITOR
         * =========================
         */

        drawRowLabel(
                "HUD EDITOR",
                contentX,
                panelTop + 120
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

    @Override
    public boolean doesGuiPauseGame() {

        return false;
    }
}