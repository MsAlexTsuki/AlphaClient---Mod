// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.fighthistory.FightHistoryManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Settings for Fight History recording and saved entries. */
public final class FightHistorySettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;
    private static final int CLEAR_HISTORY = 3;

    private final GuiScreen parent;
    private int left, right, top, bottom;

    public FightHistorySettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 100);
        bottom = Math.min(height - 12, top + 200);

        buttonList.add(new AlphaButton(TOGGLE, left + 20, top + 68, 260, 24,
                "FIGHT HISTORY: " + (FightHistoryManager.isEnabled() ? "ON" : "OFF"),
                FightHistoryManager.isEnabled()));
        buttonList.add(new AlphaButton(CLEAR_HISTORY, left + 20, top + 102,
                260, 24, "CLEAR HISTORY"));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 29, 80, 20, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(left, top, right, bottom, 0xD90A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "FIGHT HISTORY SETTINGS",
                width / 2, top + 17, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Control automatic fight recording",
                width / 2, top + 42, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Clear History permanently deletes all saved players",
                width / 2, top + 143, 0xFF8E95A3);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == TOGGLE) {
            FightHistoryManager.setEnabled(!FightHistoryManager.isEnabled());
            initGui();
        } else if (button.id == CLEAR_HISTORY) {
            FightHistoryManager.clearHistory();
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
