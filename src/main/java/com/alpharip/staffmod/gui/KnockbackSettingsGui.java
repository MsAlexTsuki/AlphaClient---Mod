package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.util.KnockbackTracker;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class KnockbackSettingsGui extends ModernGuiScreen {

    private static final int BUTTON_TOGGLE = 1;
    private static final int BUTTON_EDITOR = 2;
    private static final int BUTTON_BACK = 3;

    private final GuiScreen parentScreen;

    public KnockbackSettingsGui(
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
                getResponsivePanelWidth(600);

        int panelLeft =
                centerX - panelWidth / 2;

        int panelRight =
                panelLeft + panelWidth;

        boolean small =
                isSmallScreen();

        /*
         * =========================
         * TOGGLE
         * =========================
         */

        int toggleWidth =
                Math.min(
                        120,
                        panelWidth - 20
                );

        int toggleX =
                panelRight
                        - toggleWidth
                        - 10;

        int toggleY =
                small
                        ? 120
                        : 135;

        this.buttonList.add(
                new ModernButton(
                        BUTTON_TOGGLE,
                        toggleX,
                        toggleY,
                        toggleWidth,
                        25,
                        getToggleText()
                )
        );

        /*
         * =========================
         * HUD EDITOR
         * =========================
         *
         * Direct onder de ON/OFF knop.
         * Zelfde breedte en dezelfde X-positie.
         */

        int editorY =
                toggleY + 35;

        this.buttonList.add(
                new ModernButton(
                        BUTTON_EDITOR,
                        toggleX,
                        editorY,
                        toggleWidth,
                        25,
                        "HUD EDITOR"
                )
        );

        /*
         * =========================
         * BACK
         * =========================
         *
         * Standaard BACK knop
         * voor alle Mod GUI's.
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

        return KnockbackTracker.isEnabled()
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

        /*
         * =========================
         * TOGGLE
         * =========================
         */

        if (button.id == BUTTON_TOGGLE) {

            KnockbackTracker.toggle();

            button.displayString =
                    getToggleText();

            return;
        }

        /*
         * =========================
         * HUD EDITOR
         * =========================
         */

        if (button.id == BUTTON_EDITOR) {

            this.mc.displayGuiScreen(
                    new HudEditorGui(this)
            );

            return;
        }

        /*
         * =========================
         * BACK
         * =========================
         */

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

        /*
         * =====================================================
         * TRANSPARENT WORLD BACKGROUND
         * =====================================================
         */

        drawModernBackground();

        int centerX =
                this.width / 2;

        boolean small =
                isSmallScreen();

        /*
         * =====================================================
         * TITLE
         * =====================================================
         */

        drawModTitle(
                "KNOCKBACK INDICATOR",
                centerX,
                small
                        ? 28
                        : 50
        );

        int panelWidth =
                getResponsivePanelWidth(600);

        int panelLeft =
                centerX - panelWidth / 2;

        int panelRight =
                panelLeft + panelWidth;

        int panelTop =
                small
                        ? 85
                        : 105;

        int panelBottom =
                small
                        ? 285
                        : 305;

        /*
         * =====================================================
         * TRANSPARENT MAIN AREA
         * =====================================================
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
         * KNOCKBACK TRACKER
         * =========================
         */

        drawRowLabel(
                "KNOCKBACK TRACKER",
                contentX,
                panelTop + 20
        );

        this.fontRendererObj.drawStringWithShadow(
                "Measures expected versus observed player knockback.",
                contentX,
                panelTop + 43,
                COLOR_GRAY
        );

        /*
         * =========================
         * STATUS
         * =========================
         */

        this.fontRendererObj.drawStringWithShadow(
                "Status:",
                contentX,
                panelTop + 67,
                COLOR_WHITE
        );

        this.fontRendererObj.drawStringWithShadow(
                KnockbackTracker.isEnabled()
                        ? "ENABLED"
                        : "DISABLED",
                contentX + 60,
                panelTop + 67,
                KnockbackTracker.isEnabled()
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
                panelTop + 95
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