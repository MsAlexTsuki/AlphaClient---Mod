package com.alpharip.staffmod.singleplayer.autoreplant;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Compact settings screen for the singleplayer Auto Replant module. */
public final class AutoReplantGui extends AlphaGuiScreen {
    private final GuiScreen parent;
    private int left;
    private int top;
    private int panelWidth;

    public AutoReplantGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(320, width - 20);
        left = (width - panelWidth) / 2;
        top = (height - 150) / 2;

        boolean enabled = AutoReplantSettings.isEnabled();
        buttonList.add(new AlphaButton(1, left + 16, top + 52, panelWidth - 32, 24,
                "Auto Replant: " + (enabled ? "ON" : "OFF"), enabled, enabled));
        buttonList.add(new AlphaButton(2, left + 16, top + 108, panelWidth - 32, 20, "DONE"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) {
            AutoReplantSettings.setEnabled(!AutoReplantSettings.isEnabled());
        } else if (button.id == 2) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + 150, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "AUTO REPLANT", width / 2, top + 15, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER", width / 2, top + 32, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Vanilla Minecraft 1.8.9 crops only", width / 2,
                top + 86, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
