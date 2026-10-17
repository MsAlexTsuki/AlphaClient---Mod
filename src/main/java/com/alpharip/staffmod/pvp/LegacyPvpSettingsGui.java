package com.alpharip.staffmod.pvp;

import com.alpharip.staffmod.gui.ModernButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Settings screen for the toggleable Legacy PvP module. */
public final class LegacyPvpSettingsGui extends GuiScreen {
    private final GuiScreen parent;
    private static final int TOGGLE_VIEW_BOBBING = 1;
    private static final int CLOSE = 2;

    public LegacyPvpSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        LegacyPvpSettings.load();
        int centerX = width / 2;
        buttonList.add(new ModernButton(TOGGLE_VIEW_BOBBING, centerX - 110, 112, 220, 22,
                viewBobbingLabel()));
        buttonList.add(new ModernButton(CLOSE, centerX - 70, 176, 140, 22, "BACK"));
    }

    private String viewBobbingLabel() {
        return "Disable Camera Bobbing: " + (LegacyPvpSettings.isDisableViewBobbing() ? "ON" : "OFF");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == TOGGLE_VIEW_BOBBING) {
            LegacyPvpSettings.setDisableViewBobbing(!LegacyPvpSettings.isDisableViewBobbing());
            initGui();
        } else if (button.id == CLOSE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x88000000);
        int centerX = width / 2;
        drawRect(centerX - 155, 76, centerX + 155, 222, 0xDD0C0C0C);
        drawRect(centerX - 155, 76, centerX + 155, 77, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "LEGACY PVP SETTINGS", centerX, 88, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "Choose which supported legacy option to use.", centerX, 99, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Legacy 1.7 blockhit animation is not wired yet.", centerX, 146, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "This option only changes camera bobbing.", centerX, 157, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
