package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.ping.PingMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Appearance settings for the Ping HUD; module enablement remains in the module menu. */
public final class PingSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int BACKGROUND = 3;
    private static final int COLOR = 5;
    private final GuiScreen parent;
    private int left, top, right, bottom;

    public PingSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 80);
        bottom = Math.min(height - 12, top + 160);
        buttonList.add(new AlphaButton(BACKGROUND, left + 20, top + 58, 260, 20, backgroundLabel()));
        buttonList.add(new AlphaButton(COLOR, left + 20, top + 86, 260, 20, "TEXT COLOR: " + PingMod.getColorName()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    private String backgroundLabel() {
        return "HUD BACKGROUND: " + (PingMod.isShowBackground() ? "ON" : "OFF");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "PING SETTINGS", width / 2, top + 15, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Configure the Ping HUD appearance", width / 2, top + 34, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case BACK:
                mc.displayGuiScreen(parent);
                return;
            case BACKGROUND:
                PingMod.setShowBackground(!PingMod.isShowBackground());
                break;
            case COLOR:
                PingMod.cycleColor();
                break;
            default:
                return;
        }
        initGui();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
