package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Explains where Minecraft account sessions are managed.
 * Minecraft authentication is owned by the launcher; this screen must not collect
 * or persist Microsoft passwords inside the game client.
 */
public final class AccountInfoGui extends GuiScreen {
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
        int panelWidth = Math.min(300, width - 24);
        int left = (width - panelWidth) / 2;
        int top = height / 2 - 62;
        drawRect(left, top, left + panelWidth, top + 126, 0xE814171D);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFD866);
        drawRect(left, top, left + 3, top + 126, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "MINECRAFT ACCOUNTS", width / 2, top + 15, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Add and switch Microsoft accounts", width / 2, top + 39, 0xFFF0F1F4);
        drawCenteredString(fontRendererObj, "in the AlphaLauncher account manager.", width / 2, top + 53, 0xFFADB3C0);
        drawCenteredString(fontRendererObj, "The game client does not store passwords.", width / 2, top + 70, 0xFF858C9A);
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
