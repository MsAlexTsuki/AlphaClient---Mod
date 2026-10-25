package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.AfkTimerHud;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Settings for the AFK timer, opened from the Quality page. */
public final class AfkTimerSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLE = 2;
    private static final int DISPLAY_MODE = 3;
    private static final int BACKGROUND = 4;
    private static final int COLOR = 5;

    private final GuiScreen parent;
    private int left, top, right, bottom;

    public AfkTimerSettingsGui(GuiScreen parent) {
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
                "AFK TIMER: " + (AfkTimerHud.isEnabled() ? "ON" : "OFF"), AfkTimerHud.isEnabled()));
        buttonList.add(new AlphaButton(DISPLAY_MODE, left + 20, top + 86, 260, 20,
                "DISPLAY: " + AfkTimerHud.getDisplayModeName()));
        buttonList.add(new AlphaButton(BACKGROUND, left + 20, top + 114, 260, 20,
                "HUD BACKGROUND: " + (AfkTimerHud.isShowBackground() ? "ON" : "OFF"),
                AfkTimerHud.isShowBackground()));
        buttonList.add(new AlphaButton(COLOR, left + 20, top + 140, 260, 20,
                "TEXT COLOR: " + AfkTimerHud.getColorName()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "AFK TIMER", width / 2, top + 15, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Detects player position, not mouse movement", width / 2, top + 34, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "AFK detection starts after 10 seconds without moving", width / 2,
                bottom - 43, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case BACK:
                mc.displayGuiScreen(parent);
                return;
            case ENABLE:
                AfkTimerHud.setEnabled(!AfkTimerHud.isEnabled());
                break;
            case DISPLAY_MODE:
                AfkTimerHud.cycleDisplayMode();
                break;
            case BACKGROUND:
                AfkTimerHud.setShowBackground(!AfkTimerHud.isShowBackground());
                break;
            case COLOR:
                AfkTimerHud.cycleColor();
                break;
            default:
                return;
        }
        initGui();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
