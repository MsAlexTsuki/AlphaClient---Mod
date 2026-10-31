package com.alpharip.staffmod.AutoTool;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

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
        panelWidth = Math.max(1, Math.min(320, width - 16));
        panelHeight = 128;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int buttonWidth = Math.max(1, panelWidth - 32);
        int doneWidth = Math.min(76, Math.max(1, panelWidth - 16));
        int doneX = left + panelWidth - doneWidth - 16;

        buttonList.add(new GuiButton(1, left + 16, top + 42, buttonWidth, 20,
                "AutoTool: " + onOff(AutoToolSettings.isEnabled())));
        buttonList.add(new GuiButton(2, left + 16, top + 66, buttonWidth, 20,
                "Switch back after mining: " + onOff(AutoToolSettings.isSwapBackEnabled())));
        buttonList.add(new GuiButton(3, doneX, top + panelHeight - 25, doneWidth, 18, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) {
            AutoToolSettings.setEnabled(!AutoToolSettings.isEnabled());
        } else if (button.id == 2) {
            AutoToolSettings.setSwapBackEnabled(!AutoToolSettings.isSwapBackEnabled());
        } else if (button.id == 3) {
            closeToParent();
            return;
        }
        initGui();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            closeToParent();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void closeToParent() {
        if (mc != null) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawString(fontRendererObj, "AUTOTOOL", left + 16, top + 13, 0xFFF2F3F5);
        drawString(fontRendererObj, "Selects the best suitable hotbar item", left + 16,
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
