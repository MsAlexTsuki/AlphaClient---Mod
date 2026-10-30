// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.fighthistory.FightHistoryManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Displays the player's locally saved, per-opponent fight history. */
public final class FightHistoryGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private final GuiScreen parent;
    private int panelLeft, panelRight, panelTop, panelBottom;
    private int scrollOffset;
    private int visibleRows;
    private List<FightHistoryManager.FightEntry> visibleEntries;

    public FightHistoryGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = Math.min(340, width - 24);
        int panelHeight = Math.min(260, height - 24);
        panelLeft = (width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;
        panelTop = (height - panelHeight) / 2;
        panelBottom = panelTop + panelHeight;

        List<FightHistoryManager.FightEntry> entries = FightHistoryManager.getEntries();
        int listTop = panelTop + 58;
        int listBottom = panelBottom - 48;
        int rowHeight = 30;
        visibleRows = Math.min(5, Math.max(1, (listBottom - listTop) / rowHeight));
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, entries.size() - visibleRows)));
        int end = Math.min(entries.size(), scrollOffset + visibleRows);
        visibleEntries = entries.subList(scrollOffset, end);
        buttonList.add(new AlphaButton(BACK, panelLeft + 10, panelBottom - 29, 80, 20, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "FIGHT HISTORY", width / 2, panelTop + 14, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "PLAYER FIGHT HISTORY",
                width / 2, panelTop + 32, 0xFFFFD866);

        List<FightHistoryManager.FightEntry> entries = FightHistoryManager.getEntries();
        int listTop = panelTop + 58;
        int rowHeight = 30;
        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No fights recorded yet",
                    width / 2, listTop + 38, 0xFFB8BDC8);
            drawCenteredString(fontRendererObj, "Hit a player to add them to your history",
                    width / 2, listTop + 53, 0xFF8E95A3);
        } else {
            for (int i = 0; i < visibleEntries.size(); i++) {
                FightHistoryManager.FightEntry entry = visibleEntries.get(i);
                int y = listTop + i * rowHeight;
                int rowLeft = panelLeft + 12;
                int rowRight = panelRight - 12;
                drawRect(rowLeft, y, rowRight, y + 27, 0x9914171E);
                drawRect(rowLeft, y, rowRight, y + 1, 0x554B515D);
                drawString(fontRendererObj, entry.getPlayerName(), rowLeft + 8, y + 5, 0xFFF3F4F6);
                drawString(fontRendererObj, "Fight Count: " + entry.getFightCount(),
                        rowLeft + 8, y + 19, 0xFFFFD866);
                drawString(fontRendererObj, "Last Seen: " + formatTime(entry.getLastSeenAt()),
                        rowLeft + 112, y + 19, 0xFFB8BDC8);
                if (entry.isActive()) {
                    drawString(fontRendererObj, "ACTIVE", rowRight - 48, y + 5, 0xFF9CE2AC);
                }
            }
        }
        drawString(fontRendererObj, entries.size() + " PLAYERS RECORDED",
                panelLeft + 12, panelBottom - 40, 0xFFB8BDC8);
        if (entries.size() > visibleRows) {
            drawString(fontRendererObj, "Scroll to view all players",
                    panelRight - 170, panelBottom - 40, 0xFF8E95A3);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("dd MMM · HH:mm", Locale.ENGLISH).format(new Date(timestamp));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK) mc.displayGuiScreen(parent);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getDWheel();
        super.handleMouseInput();
        if (wheel != 0) {
            int maxScroll = Math.max(0, FightHistoryManager.getEntries().size() - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
