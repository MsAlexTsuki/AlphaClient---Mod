package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Account-switcher information screen.
 * Microsoft authentication and saved account tokens remain managed by AlphaLauncher.
 */
public final class AccountInfoGui extends AlphaGuiScreen {
    private final GuiScreen parent;

    public AccountInfoGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new AlphaButton(0, width / 2 - 72, height / 2 + 42, 144, 22, "BACK", true));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(320, width - 24);
        int left = (width - panelWidth) / 2;
        int top = height / 2 - 66;
        drawRect(left, top, left + panelWidth, top + 132, 0xE814171D);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFD866);
        drawRect(left, top, left + 3, top + 132, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ACCOUNT SWITCHER", width / 2, top + 15, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Manage saved Microsoft accounts", width / 2, top + 40, 0xFFF0F1F4);
        drawCenteredString(fontRendererObj, "in the AlphaLauncher account menu.", width / 2, top + 54, 0xFFADB3C0);
        drawCenteredString(fontRendererObj, "Select an account before launching Minecraft.", width / 2, top + 75, 0xFFADB3C0);
        drawCenteredString(fontRendererObj, "Account passwords are never stored by the mod.", width / 2, top + 91, 0xFF858C9A);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == 0) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
