package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class ModSettingsGui extends GuiScreen {

    private static final int BUTTON_CPS = 1;
    private static final int BUTTON_BHOP = 2;
    private static final int BUTTON_KNOCKBACK = 3;
    private static final int BUTTON_RANKS = 4;
    private static final int BUTTON_CLOSE = 5;

    @Override
    public void initGui() {

        this.buttonList.clear();

        int centerX =
                this.width / 2;

        /*
         * =========================
         * CPS HUD
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_CPS,
                        centerX - 250,
                        145,
                        150,
                        24,
                        "CPS HUD"
                )
        );

        /*
         * =========================
         * BHOP
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_BHOP,
                        centerX - 75,
                        145,
                        150,
                        24,
                        "BHOP"
                )
        );

        /*
         * =========================
         * KNOCKBACK
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_KNOCKBACK,
                        centerX + 100,
                        145,
                        150,
                        24,
                        "KNOCKBACK"
                )
        );

        /*
         * =========================
         * RANKS
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_RANKS,
                        centerX - 75,
                        205,
                        150,
                        24,
                        "RANKS"
                )
        );

        /*
         * =========================
         * CLOSE
         * =========================
         */

        this.buttonList.add(
                new ModernButton(
                        BUTTON_CLOSE,
                        centerX - 100,
                        250,
                        200,
                        24,
                        "CLOSE"
                )
        );
    }

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        /*
         * =========================
         * CPS
         * =========================
         */

        if (button.id == BUTTON_CPS) {

            this.mc.displayGuiScreen(
                    new CpsSettingsGui(this)
            );

            return;
        }

        /*
         * =========================
         * BHOP
         * =========================
         */

        if (button.id == BUTTON_BHOP) {

            this.mc.displayGuiScreen(
                    new BhopSettingsGui(this)
            );

            return;
        }

        /*
         * =========================
         * KNOCKBACK
         * =========================
         */

        if (button.id == BUTTON_KNOCKBACK) {

            this.mc.displayGuiScreen(
                    new KnockbackSettingsGui(this)
            );

            return;
        }

        /*
         * =========================
         * RANKS
         * =========================
         */

        if (button.id == BUTTON_RANKS) {

            this.mc.displayGuiScreen(
                    new RankSettingsGui(this)
            );

            return;
        }

        /*
         * =========================
         * CLOSE
         * =========================
         */

        if (button.id == BUTTON_CLOSE) {

            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks) {

        /*
         * Transparante donkere overlay.
         */

        drawRect(
                0,
                0,
                this.width,
                this.height,
                0x88000000
        );

        int centerX =
                this.width / 2;

        /*
         * =========================
         * MAIN PANEL
         * =========================
         */

        int left =
                centerX - 285;

        int right =
                centerX + 285;

        int top = 55;
        int bottom = 285;

        drawRect(
                left,
                top,
                right,
                bottom,
                0xDD0C0C0C
        );

        /*
         * =========================
         * TOP BORDER
         * =========================
         */

        drawRect(
                left,
                top,
                right,
                top + 1,
                0xFF444444
        );

        /*
         * =========================
         * BOTTOM BORDER
         * =========================
         */

        drawRect(
                left,
                bottom - 1,
                right,
                bottom,
                0xFF252525
        );

        /*
         * =========================
         * TITLE
         * =========================
         */

        drawCenteredString(
                this.fontRendererObj,
                "ALPHA'S STAFFING MODS",
                centerX,
                72,
                0xFFFFFF
        );

        drawCenteredString(
                this.fontRendererObj,
                "Configure your mods",
                centerX,
                88,
                0x999999
        );

        /*
         * =========================
         * DIVIDER
         * =========================
         */

        drawRect(
                centerX - 250,
                108,
                centerX + 250,
                109,
                0xFF303030
        );

        /*
         * =========================
         * CATEGORY HEADERS
         * =========================
         */

        drawCenteredString(
                this.fontRendererObj,
                "COMBAT / HUD",
                centerX - 175,
                119,
                0x55FF55
        );

        drawCenteredString(
                this.fontRendererObj,
                "MOVEMENT",
                centerX,
                119,
                0x55FF55
        );

        drawCenteredString(
                this.fontRendererObj,
                "DETECTION",
                centerX + 175,
                119,
                0x55FF55
        );

        /*
         * =========================
         * DESCRIPTIONS
         * =========================
         */

        drawCenteredString(
                this.fontRendererObj,
                "Target HUD",
                centerX - 175,
                178,
                0xAAAAAA
        );

        drawCenteredString(
                this.fontRendererObj,
                "Movement",
                centerX,
                178,
                0xAAAAAA
        );

        drawCenteredString(
                this.fontRendererObj,
                "Player analysis",
                centerX + 175,
                178,
                0xAAAAAA
        );

        /*
         * =========================
         * RANK DESCRIPTION
         * =========================
         */

        drawCenteredString(
                this.fontRendererObj,
                "Staff nametags",
                centerX,
                238,
                0xAAAAAA
        );

        /*
         * =========================
         * KEYBIND HINT
         * =========================
         */

        drawCenteredString(
                this.fontRendererObj,
                "Right Control",
                centerX,
                bottom + 12,
                0x666666
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