package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Legacy settings entry point. Only BHOP remains available here;
 * the other legacy modules are no longer exposed in this menu.
 */
public class ModSettingsGui extends AlphaGuiScreen {

    private static final int BUTTON_BHOP = 2;
    private static final int BUTTON_CLOSE = 5;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int centerX = this.width / 2;

        this.buttonList.add(new ModernButton(
                BUTTON_BHOP, centerX - 75, 145, 150, 24, "BHOP"
        ));
        this.buttonList.add(new ModernButton(
                BUTTON_CLOSE, centerX - 100, 205, 200, 24, "CLOSE"
        ));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_BHOP) {
            this.mc.displayGuiScreen(new BhopSettingsGui(this));
        } else if (button.id == BUTTON_CLOSE) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, 0x88000000);
        int centerX = this.width / 2;
        int left = centerX - 180;
        int right = centerX + 180;
        int top = 75;
        int bottom = 240;

        drawRect(left, top, right, bottom, 0xDD0C0C0C);
        drawRect(left, top, right, top + 1, 0xFF444444);
        drawRect(left, bottom - 1, right, bottom, 0xFF252525);

        drawCenteredString(this.fontRendererObj, "ALPHA CLIENT SETTINGS", centerX, 94, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, "Configure BHOP", centerX, 112, 0xFFB8BDC8);
        drawRect(centerX - 145, 132, centerX + 145, 133, 0xFF303030);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}