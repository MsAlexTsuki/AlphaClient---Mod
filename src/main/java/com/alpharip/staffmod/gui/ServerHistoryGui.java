// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Visual layout for the planned Server History screen.
 *
 * The server rows are presentation examples only. History tracking, persistence,
 * joining servers, clearing records and settings are not implemented here.
 */
public final class ServerHistoryGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int JOIN_HYPIXEL = 2;
    private static final int JOIN_MINEMEN = 3;
    private static final int JOIN_PRACTICE = 4;
    private static final int CLEAR_HISTORY = 5;
    private static final int SETTINGS = 6;

    private final GuiScreen parent;
    private int panelLeft, panelRight, panelTop, panelBottom;

    public ServerHistoryGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 460);
        panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 300);

        int rowHeight = 43;
        int rowTop = panelTop + 66;
        int joinX = panelRight - 82;
        int joinWidth = 70;

        addPreviewJoin(JOIN_HYPIXEL, joinX, rowTop + 8, joinWidth, rowHeight);
        addPreviewJoin(JOIN_MINEMEN, joinX, rowTop + 57, joinWidth, rowHeight);
        addPreviewJoin(JOIN_PRACTICE, joinX, rowTop + 106, joinWidth, rowHeight);

        AlphaButton clear = new AlphaButton(CLEAR_HISTORY, panelLeft + 12,
                panelBottom - 29, 112, 20, "CLEAR HISTORY");
        clear.enabled = false;
        buttonList.add(clear);

        buttonList.add(new AlphaButton(BACK, width / 2 - 40,
                panelBottom - 29, 80, 20, "BACK"));

        AlphaButton settings = new AlphaButton(SETTINGS, panelRight - 92,
                panelBottom - 29, 80, 20, "SETTINGS");
        settings.enabled = false;
        buttonList.add(settings);
    }

    private void addPreviewJoin(int id, int x, int y, int buttonWidth, int rowHeight) {
        AlphaButton join = new AlphaButton(id, x, y + (rowHeight - 22) / 2,
                buttonWidth, 22, "JOIN", true);
        join.enabled = false;
        buttonList.add(join);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, 0xFFFFD866);

        drawCenteredString(fontRendererObj, "SERVER HISTORY",
                width / 2, panelTop + 14, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "PREVIOUSLY CONNECTED SERVERS",
                width / 2, panelTop + 32, 0xFFFFD866);

        drawServerRow(panelTop + 66, "Hypixel", "mc.hypixel.net", "Today · 20:42");
        drawServerRow(panelTop + 115, "Minemen Club", "na.minemen.club", "Yesterday · 18:15");
        drawServerRow(panelTop + 164, "Practice Server", "play.example.net", "26 October · 16:30");

        drawString(fontRendererObj, "3 / 20 SERVERS SAVED",
                panelLeft + 12, panelBottom - 48, 0xFFB8BDC8);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawServerRow(int y, String name, String address, String lastVisited) {
        int rowLeft = panelLeft + 12;
        int rowRight = panelRight - 12;
        int rowBottom = y + 43;

        drawRect(rowLeft, y, rowRight, rowBottom, 0x9914171E);
        drawRect(rowLeft, y, rowRight, y + 1, 0x554B515D);
        drawString(fontRendererObj, name, rowLeft + 10, y + 7, 0xFFF3F4F6);
        drawString(fontRendererObj, address, rowLeft + 10, y + 21, 0xFFB8BDC8);
        drawString(fontRendererObj, lastVisited, rowRight - 104, y + 7, 0xFF8E95A3);
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
