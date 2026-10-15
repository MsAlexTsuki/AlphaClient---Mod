package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** AlphaClient settings hub; Minecraft's native options remain available as a sub-screen. */
public final class AlphaSettingsMenu extends GuiScreen {
    private static final int BACK = 1;
    private static final int MINECRAFT_OPTIONS = 4;
    private final GuiScreen parent;

    public AlphaSettingsMenu(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int panelWidth = Math.min(360, width - 24);
        int left = center - panelWidth / 2;
        int top = 74;
        int buttonWidth = panelWidth - 28;

        buttonList.add(new AlphaButton(MINECRAFT_OPTIONS, left + 14, top + 58, buttonWidth, 24,
                "MINECRAFT OPTIONS"));
        buttonList.add(new AlphaButton(BACK, 10, height - 25, 76, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xFF181B22, 0xFF090A0E);
        int panelWidth = Math.min(360, width - 24);
        int left = width / 2 - panelWidth / 2;
        int right = left + panelWidth;
        int top = 20;
        drawRect(left, top, right, Math.min(height - 34, top + 252), 0xFF14171D);
        drawRect(left, top, right, top + 1, 0xFF343944);
        drawRect(left, top, left + 3, Math.min(height - 34, top + 252), 0xFFFFD866);

        drawString(fontRendererObj, "ALPHA CLIENT", left + 14, top + 10, 0xFFFFD866);
        drawString(fontRendererObj, "Settings", left + 14, top + 24, 0xFFF4F5F8);
        drawString(fontRendererObj, "Game options and client preferences.",
                left + 14, top + 37, 0xFF9298A5);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        switch (button.id) {
            case BACK:
                minecraft.displayGuiScreen(parent);
                break;
            case MINECRAFT_OPTIONS:
                minecraft.displayGuiScreen(new GuiOptions(this, minecraft.gameSettings));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
