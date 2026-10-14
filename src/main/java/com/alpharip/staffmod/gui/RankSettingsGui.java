package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.ranks.PlayerRankManager;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class RankSettingsGui extends ModernGuiScreen {

    private static final int BUTTON_TOGGLE = 1;
    private static final int BUTTON_RELOAD = 2;
    private static final int BUTTON_BACK = 3;

    private final GuiScreen parentScreen;

    public RankSettingsGui(
            GuiScreen parentScreen) {

        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {

        this.buttonList.clear();

        int centerX =
                this.width / 2;

        /*
         * =========================
         * TOGGLE
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_TOGGLE,
                        centerX + 55,
                        112,
                        65,
                        20,
                        getToggleText()
                )
        );

        /*
         * =========================
         * RELOAD
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_RELOAD,
                        centerX - 90,
                        235,
                        180,
                        21,
                        "RELOAD NAME TAGS"
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
                        centerX - 90,
                        263,
                        180,
                        21,
                        "BACK"
                )
        );
    }

    private String getToggleText() {

        return PlayerRankManager.isEnabled()
                ? "ON"
                : "OFF";
    }

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        if (button.id == BUTTON_TOGGLE) {

            PlayerRankManager.toggle();

            button.displayString =
                    getToggleText();

            return;
        }

        if (button.id == BUTTON_RELOAD) {

            PlayerRankManager.reload();

            return;
        }

        if (button.id == BUTTON_BACK) {

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
         * HEADER
         * =========================
         */

        drawHeader(
                "NAME TAGS",
                ""
        );

        /*
         * =========================
         * PANEL
         * =========================
         */

        int panelWidth = 320;

        int panelLeft =
                centerX - panelWidth / 2;

        int panelRight =
                centerX + panelWidth / 2;

        int panelTop = 100;
        int panelBottom = 292;

        drawPanel(
                panelLeft,
                panelTop,
                panelRight,
                panelBottom
        );

        /*
         * =========================
         * TITLE
         * =========================
         */

        this.fontRendererObj.drawStringWithShadow(
                "NAME TAGS",
                panelLeft + 16,
                114,
                COLOR_WHITE
        );

        /*
         * =========================
         * DESCRIPTION
         * =========================
         */

        this.fontRendererObj.drawStringWithShadow(
                "Shows staff rank prefixes above player names.",
                panelLeft + 16,
                136,
                COLOR_GRAY
        );

        /*
         * =========================
         * STATUS
         * =========================
         */

        this.fontRendererObj.drawStringWithShadow(
                "Status",
                panelLeft + 16,
                164,
                COLOR_GRAY
        );

        this.fontRendererObj.drawStringWithShadow(
                PlayerRankManager.isEnabled()
                        ? "ENABLED"
                        : "DISABLED",
                panelLeft + 70,
                164,
                PlayerRankManager.isEnabled()
                        ? COLOR_GREEN
                        : 0xFFFF5555
        );

        /*
         * =========================
         * SERVER
         * =========================
         */

        this.fontRendererObj.drawStringWithShadow(
                "Server",
                panelLeft + 165,
                164,
                COLOR_GRAY
        );

        this.fontRendererObj.drawStringWithShadow(
                PlayerRankManager.isOnAlphaRipServer()
                        ? "Online on Alpha"
                        : "Offline on Alpha",
                panelLeft + 205,
                164,
                PlayerRankManager.isOnAlphaRipServer()
                        ? COLOR_GREEN
                        : 0xFFFF5555
        );

        /*
         * =========================
         * DIVIDER
         * =========================
         */

        drawRect(
                panelLeft + 16,
                187,
                panelRight - 16,
                188,
                0xFF303030
        );

        /*
         * =========================
         * EDIT INFO
         * =========================
         */

        this.fontRendererObj.drawStringWithShadow(
                "EDIT NAME TAGS",
                panelLeft + 16,
                201,
                COLOR_WHITE
        );

        this.fontRendererObj.drawStringWithShadow(
                "config/alphastaffmod/ranks.json, then reload.",
                panelLeft + 16,
                219,
                COLOR_GRAY
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