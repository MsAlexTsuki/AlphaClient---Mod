package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;

/** Settings for the cumulative Playtime Tracker, opened from Quality. */
public final class PlaytimeTrackerSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLE = 2;
    private static final int BACKGROUND = 3;
    private static final int COLORS = 4;

    private final net.minecraft.client.gui.GuiScreen parent;
    private int left, top, right, bottom;

    public PlaytimeTrackerSettingsGui(net.minecraft.client.gui.GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 115);
        bottom = Math.min(height - 12, top + 230);

        buttonList.add(new AlphaButton(ENABLE, left + 20, top + 58, 260, 20,
                "PLAYTIME TRACKER HUD: " + (PlaytimeTrackerHud.isEnabled() ? "ON" : "OFF"),
                PlaytimeTrackerHud.isEnabled()));
        buttonList.add(new AlphaButton(BACKGROUND, left + 20, top + 84, 260, 20,
                "HUD BACKGROUND: " + (PlaytimeTrackerHud.isShowBackground() ? "ON" : "OFF"),
                PlaytimeTrackerHud.isShowBackground()));
        buttonList.add(new AlphaButton(COLORS, left + 20, top + 110, 260, 20,
                "HUD COLOR: " + PlaytimeTrackerHud.getColorName()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "PLAYTIME TRACKER", width / 2, top + 17, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Tracks Minecraft runtime even when its HUD is off",
                width / 2, top + 37, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Move the HUD in the HUD Editor",
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
            PlaytimeTrackerHud.setEnabled(!PlaytimeTrackerHud.isEnabled());
            initGui();
        } else if (button.id == BACKGROUND) {
            PlaytimeTrackerHud.setShowBackground(!PlaytimeTrackerHud.isShowBackground());
            initGui();
        } else if (button.id == COLORS) {
            PlaytimeTrackerHud.cycleColor();
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
