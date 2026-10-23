package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.SessionTimerHud;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;

/** Settings for the Session Timer, opened from the Quality page. */
public final class SessionTimerSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLE = 2;
    private static final int BACKGROUND = 3;
    private static final int COLORS = 4;

    private final net.minecraft.client.gui.GuiScreen parent;
    private int left, top, right, bottom;

    public SessionTimerSettingsGui(net.minecraft.client.gui.GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 105);
        bottom = Math.min(height - 12, top + 210);

        buttonList.add(new AlphaButton(ENABLE, left + 20, top + 58, 260, 20,
                "SESSION TIMER: " + (SessionTimerHud.isEnabled() ? "ON" : "OFF"),
                SessionTimerHud.isEnabled()));
        buttonList.add(new AlphaButton(BACKGROUND, left + 20, top + 84, 260, 20,
                "HUD BACKGROUND: " + (SessionTimerHud.isShowBackground() ? "ON" : "OFF"),
                SessionTimerHud.isShowBackground()));
        buttonList.add(new AlphaButton(COLORS, left + 20, top + 110, 260, 20, "HUD COLOR: " + SessionTimerHud.getColorName()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "SESSION TIMER", width / 2, top + 17, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Tracks the entire Minecraft runtime",
                width / 2, top + 37, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Move it in the HUD Editor; choose its color below",
                width / 2, bottom - 43, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == ENABLE) {
            SessionTimerHud.setEnabled(!SessionTimerHud.isEnabled());
            initGui();
        } else if (button.id == BACKGROUND) {
            SessionTimerHud.setShowBackground(!SessionTimerHud.isShowBackground());
            initGui();
        } else if (button.id == COLORS) {
            SessionTimerHud.cycleColor();
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
