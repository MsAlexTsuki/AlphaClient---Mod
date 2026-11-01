// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * Dedicated browser for AlphaClient mods intended for singleplayer worlds.
 * Singleplayer-only modules will be added to this screen as they are registered.
 */
public final class SingleplayerGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private int panelLeft, panelRight, panelTop, panelBottom;

    public SingleplayerGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 460);
        panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 300);

        buttonList.add(new AlphaButton(BACK, panelLeft + 10, panelBottom - 29, 90, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button != null && button.id == BACK) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER", width / 2,
                panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER MODS", width / 2,
                panelTop + 30, ACCENT);
        drawString(fontRendererObj, "Mods intended for singleplayer worlds",
                panelLeft + 12, panelTop + 52, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "No singleplayer mods have been added yet.",
                width / 2, panelTop + 105, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
