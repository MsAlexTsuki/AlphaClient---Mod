package com.alpharip.staffmod.AutoTool;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** In-game settings screen for AutoTool. */
public final class AutoToolGui extends GuiScreen {
    private final GuiScreen parent;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public AutoToolGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(320, Math.max(250, width - 24));
        panelHeight = 128;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int buttonWidth = panelWidth - 32;

        buttonList.add(new GuiButton(1, left + 16, top + 42, buttonWidth, 20,
                "AutoTool: " + onOff(AutoToolSettings.isEnabled())));
        buttonList.add(new GuiButton(2, left + 16, top + 66, buttonWidth, 20,
                "Switch back after mining: " + onOff(AutoToolSettings.isSwapBackEnabled())));
        buttonList.add(new GuiButton(3, left + panelWidth - 92, top + panelHeight - 25, 76, 18, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) {
            AutoToolSettings.setEnabled(!AutoToolSettings.isEnabled());
        } else if (button.id == 2) {
            AutoToolSettings.setSwapBackEnabled(!AutoToolSettings.isSwapBackEnabled());
        } else if (button.id == 3) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawString(fontRendererObj, "AUTOTOOL", left + 16, top + 13, 0xFFF2F3F5);
        drawString(fontRendererObj, "Selects the strongest item in your hotbar", left + 16,
                top + 29, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String onOff(boolean value) {
        return value ? "On" : "Off";
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
