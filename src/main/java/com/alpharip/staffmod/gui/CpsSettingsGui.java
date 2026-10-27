// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.cps.CpsMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** CPS HUD settings with a click-to-expand display-mode dropdown. */
public final class CpsSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1, BACKGROUND = 2, COLOR = 3, MODE = 4;
    private static final int OPTION_BOTH = 10, OPTION_LEFT = 11, OPTION_RIGHT = 12;
    private final GuiScreen parent;
    private int left, top, right, bottom;
    private boolean dropdownOpen;

    public CpsSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 150;
        right = left + 300;
        top = Math.max(12, height / 2 - 108);
        bottom = Math.min(height - 12, top + 216);
        buttonList.add(new AlphaButton(BACKGROUND, left + 20, top + 58, 260, 20, backgroundLabel()));
        buttonList.add(new AlphaButton(COLOR, left + 20, top + 86, 260, 20, "TEXT COLOR: " + CpsMod.getColorName()));
        buttonList.add(new AlphaButton(MODE, left + 20, top + 114, 260, 20,
                CpsMod.getDisplayModeName() + (dropdownOpen ? "  ▲" : "  ▼")));
        if (dropdownOpen) {
            buttonList.add(new AlphaButton(OPTION_BOTH, left + 20, top + 136, 260, 18, "LMB + RMB",
                    CpsMod.getDisplayMode() == CpsMod.MODE_BOTH));
            buttonList.add(new AlphaButton(OPTION_LEFT, left + 20, top + 154, 260, 18, "LMB",
                    CpsMod.getDisplayMode() == CpsMod.MODE_LEFT));
            buttonList.add(new AlphaButton(OPTION_RIGHT, left + 20, top + 172, 260, 18, "RMB",
                    CpsMod.getDisplayMode() == CpsMod.MODE_RIGHT));
            bottom = Math.min(height - 8, top + 216);
        }
        buttonList.add(new AlphaButton(BACK, left + 10, Math.min(height - 32, bottom - 24), 75, 18, "BACK"));
    }

    private String backgroundLabel() {
        return "HUD BACKGROUND: " + (CpsMod.isShowBackground() ? "ON" : "OFF");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "CPS SETTINGS", width / 2, top + 15, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Configure the CPS HUD appearance", width / 2, top + 34, 0xFFB8BDC8);
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
                CpsMod.setShowBackground(!CpsMod.isShowBackground());
                break;
            case COLOR:
                CpsMod.cycleColor();
                break;
            case MODE:
                dropdownOpen = !dropdownOpen;
                break;
            case OPTION_BOTH:
                CpsMod.setDisplayMode(CpsMod.MODE_BOTH);
                dropdownOpen = false;
                break;
            case OPTION_LEFT:
                CpsMod.setDisplayMode(CpsMod.MODE_LEFT);
                dropdownOpen = false;
                break;
            case OPTION_RIGHT:
                CpsMod.setDisplayMode(CpsMod.MODE_RIGHT);
                dropdownOpen = false;
                break;
            default:
                return;
        }
        initGui();
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
