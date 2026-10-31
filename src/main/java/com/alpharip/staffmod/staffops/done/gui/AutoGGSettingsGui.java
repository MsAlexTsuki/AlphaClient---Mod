package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.quality.AutoGGMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Small settings screen for the Auto GG Quality module. */
public final class AutoGGSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;
    private static final int DELAY_DOWN = 3;
    private static final int DELAY_UP = 4;

    private final GuiScreen parent;
    private int left, top, right, bottom;

    public AutoGGSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 95);
        bottom = Math.min(height - 12, top + 190);

        buttonList.add(new AlphaButton(TOGGLE, left + 20, top + 58, 260, 20,
                "AUTO GG: " + (AutoGGMod.isEnabled() ? "ON" : "OFF"), AutoGGMod.isEnabled()));
        buttonList.add(new AlphaButton(DELAY_DOWN, left + 20, top + 91, 38, 20, "-"));
        GuiButton delayLabel = new AlphaButton(5, left + 62, top + 91, 176, 20,
                "DELAY: " + String.format(java.util.Locale.ROOT, "%.1f", AutoGGMod.getDelaySeconds()) + "s");
        delayLabel.enabled = false;
        buttonList.add(delayLabel);
        buttonList.add(new AlphaButton(DELAY_UP, left + 242, top + 91, 38, 20, "+"));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "AUTO GG", width / 2, top + 17, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Sends GG after a detected kill or match end",
                width / 2, top + 37, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Delay is adjustable from 0 to 10 seconds",
                width / 2, bottom - 44, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == TOGGLE) {
            AutoGGMod.setEnabled(!AutoGGMod.isEnabled());
            initGui();
        } else if (button.id == DELAY_DOWN) {
            AutoGGMod.setDelaySeconds(AutoGGMod.getDelaySeconds() - 0.5D);
            initGui();
        } else if (button.id == DELAY_UP) {
            AutoGGMod.setDelaySeconds(AutoGGMod.getDelaySeconds() + 0.5D);
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
