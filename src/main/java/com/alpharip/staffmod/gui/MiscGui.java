// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Dedicated folder for miscellaneous Mods -> General features. */
public final class MiscGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;

    public MiscGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        int panelBottom = Math.min(height - 12, Math.max(180, height / 2 + 150));
        buttonList.add(new AlphaButton(BACK, panelLeft + 10, panelBottom - 29, 90, 20, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        int panelRight = Math.min(width - 12, panelLeft + 460);
        int panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        int panelBottom = Math.min(height - 12, panelTop + 300);

        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawCenteredString(fontRendererObj, "MISC", width / 2, panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "MISCELLANEOUS FEATURES", width / 2, panelTop + 30, ACCENT);
        drawCenteredString(fontRendererObj, "No miscellaneous modules have been added yet.",
                width / 2, panelTop + 86, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
