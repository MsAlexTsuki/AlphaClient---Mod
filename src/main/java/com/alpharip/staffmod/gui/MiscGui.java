// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Browser for AlphaClient miscellaneous mods. */
public final class MiscGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int SERVER_HISTORY = 2;
    private static final int FIGHT_HISTORY = 3;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreen parent;
    private int panelLeft, panelRight, panelTop, panelBottom;

    public MiscGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 460);
        panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 300);

        int gap = 8;
        int columns = 2;
        int tileWidth = (panelRight - panelLeft - 24 - gap * (columns - 1)) / columns;
        int tileHeight = 54;
        int startY = panelTop + 68;
        int startX = panelLeft + 12;

        buttonList.add(new AlphaButton(BACK, panelLeft + 10, panelBottom - 29, 90, 20, "BACK"));
        buttonList.add(new FeatureTileButton(SERVER_HISTORY, startX, startY,
                tileWidth, tileHeight, "Server History", "SH", true));
        buttonList.add(new FeatureTileButton(FIGHT_HISTORY, startX + tileWidth + gap, startY,
                tileWidth, tileHeight, "Fight History", "FH", true));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == SERVER_HISTORY) {
            mc.displayGuiScreen(new ServerHistoryGui(this));
        } else if (button.id == FIGHT_HISTORY) {
            mc.displayGuiScreen(new FightHistoryGui(this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, ACCENT);
        drawCenteredString(fontRendererObj, "MISC", width / 2, panelTop + 13, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "MISCELLANEOUS MODS", width / 2, panelTop + 30, ACCENT);
        drawString(fontRendererObj, "Features and roadmap ideas", panelLeft + 12, panelTop + 49, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
