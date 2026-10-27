// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Compatibility screen retained for the existing menu flow. Legacy utilities were removed. */
public class UtilitiesGui extends ModernGuiScreen {
    private final GuiScreen parentScreen;

    public UtilitiesGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawModernBackground();
        drawModTitle("UTILITIES", this.width / 2, 18);
        drawCenteredString(this.fontRendererObj, "No legacy utilities available", this.width / 2, this.height / 2, COLOR_WHITE);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            Minecraft.getMinecraft().displayGuiScreen(parentScreen);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            Minecraft.getMinecraft().displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
