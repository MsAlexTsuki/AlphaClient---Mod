package com.alpharip.staffmod.ComboCounter;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Alpha-styled appearance settings for the Combo Counter HUD. */
public final class ComboCounterSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int BACKGROUND = 2;
    private static final int COLOR = 3;

    private final GuiScreen parent;
    private int left, top, right, bottom;

    public ComboCounterSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 80);
        bottom = Math.min(height - 12, top + 160);

        buttonList.add(new AlphaButton(BACKGROUND, left + 20, top + 58, 260, 20,
                "HUD BACKGROUND: " + (ComboCounter.getInstance().isShowBackground() ? "ON" : "OFF")));
        buttonList.add(new AlphaButton(COLOR, left + 20, top + 86, 260, 20,
                "TEXT COLOR: " + ComboCounter.getInstance().getColorName()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "COMBO COUNTER SETTINGS", width / 2, top + 15, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Configure the Combo HUD appearance", width / 2, top + 34, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == BACKGROUND) {
            ComboCounter.getInstance().setShowBackground(!ComboCounter.getInstance().isShowBackground());
        } else if (button.id == COLOR) {
            ComboCounter.getInstance().cycleColor();
        } else {
            return;
        }
        initGui();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
