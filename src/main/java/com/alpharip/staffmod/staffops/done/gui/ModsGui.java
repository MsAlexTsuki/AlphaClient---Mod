package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Alpha's Staffing Mods
 *
 * Author: MsAlexTsuki
 */
public class ModsGui extends ModernGuiScreen {

    private static final int BUTTON_BACK = 1;

    private final GuiScreen parentScreen;

    public ModsGui(
            GuiScreen parentScreen) {

        this.parentScreen =
                parentScreen;
    }

    @Override
    public void initGui() {

        this.buttonList.clear();

        int width =
                Math.min(
                        160,
                        Math.max(
                                100,
                                this.width - 24
                        )
                );

        int backY =
                Math.max(
                        120,
                        this.height - 45
                );

        this.buttonList.add(
                new ModernButton(
                        BUTTON_BACK,
                        this.width / 2 - width / 2,
                        backY,
                        width,
                        21,
                        "BACK"
                )
        );
    }

    @Override
    protected void actionPerformed(
            GuiButton button)
            throws IOException {

        if (button == null) {
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

        drawHeader(
                "MODS",
                "Additional client modifications"
        );

        int centerX =
                this.width / 2;

        int panelWidth =
                getResponsivePanelWidth(470);

        int left =
                centerX
                        - panelWidth / 2;

        int right =
                left
                        + panelWidth;

        int top =
                isSmallScreen()
                        ? 85
                        : 98;

        int bottom =
                Math.min(
                        isSmallScreen()
                                ? 220
                                : 270,
                        this.height - 55
                );

        drawPanel(
                left,
                top,
                right,
                bottom
        );

        this.fontRendererObj.drawStringWithShadow(
                "MODS",
                left + 20,
                top + 10,
                COLOR_WHITE
        );

        this.fontRendererObj.drawStringWithShadow(
                "Additional mods can be added here.",
                left + 20,
                top + 24,
                COLOR_GRAY
        );

        drawCenteredString(
                this.fontRendererObj,
                "NO MODS AVAILABLE",
                centerX,
                top + 67,
                COLOR_GRAY
        );

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