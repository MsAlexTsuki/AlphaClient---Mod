// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.serverhistory.ServerHistoryManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Browses saved multiplayer servers and reconnects to a selected entry. */
public final class ServerHistoryGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int CLEAR_HISTORY = 2;
    private static final int SETTINGS = 3;
    private static final int JOIN_BASE = 100;

    private final GuiScreen parent;
    private final List<ServerHistoryManager.Entry> visibleEntries =
            new ArrayList<ServerHistoryManager.Entry>();
    private int scrollOffset;
    private int visibleRows;
    private int panelLeft, panelRight, panelTop, panelBottom;

    public ServerHistoryGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        visibleEntries.clear();

        panelLeft = Math.max(12, width / 2 - Math.min(230, width / 2 - 12));
        panelRight = Math.min(width - 12, panelLeft + 460);
        panelTop = Math.max(12, height / 2 - Math.min(150, height / 2 - 12));
        panelBottom = Math.min(height - 12, panelTop + 300);

        List<ServerHistoryManager.Entry> entries = ServerHistoryManager.getEntries();
        int listTop = panelTop + 58;
        int listBottom = panelBottom - 58;
        int rowHeight = 44;
        visibleRows = Math.max(1, (listBottom - listTop) / rowHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, entries.size() - visibleRows)));

        int end = Math.min(entries.size(), scrollOffset + visibleRows);
        for (int i = scrollOffset; i < end; i++) {
            int row = i - scrollOffset;
            int y = listTop + row * rowHeight;
            visibleEntries.add(entries.get(i));
            buttonList.add(new AlphaButton(JOIN_BASE + row, panelRight - 88, y + 10, 76, 22, "JOIN", true));
        }

        AlphaButton clear = new AlphaButton(CLEAR_HISTORY, panelLeft + 12,
                panelBottom - 29, 112, 20, "CLEAR HISTORY");
        clear.enabled = !entries.isEmpty();
        buttonList.add(clear);
        buttonList.add(new AlphaButton(BACK, width / 2 - 40, panelBottom - 29, 80, 20, "BACK"));
        buttonList.add(new AlphaButton(SETTINGS, panelRight - 92, panelBottom - 29,
                80, 20, "SETTINGS"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xD90A0C11);
        drawRect(panelLeft, panelTop, panelRight, panelTop + 2, 0xFFFFD866);

        drawCenteredString(fontRendererObj, "SERVER HISTORY", width / 2, panelTop + 14, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "PREVIOUSLY CONNECTED SERVERS",
                width / 2, panelTop + 32, 0xFFFFD866);

        List<ServerHistoryManager.Entry> entries = ServerHistoryManager.getEntries();
        int listTop = panelTop + 58;
        int rowHeight = 44;
        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No servers saved yet",
                    width / 2, listTop + 42, 0xFFB8BDC8);
            drawCenteredString(fontRendererObj, "Join a multiplayer server to add it here",
                    width / 2, listTop + 57, 0xFF8E95A3);
        } else {
            for (int i = 0; i < visibleEntries.size(); i++) {
                ServerHistoryManager.Entry entry = visibleEntries.get(i);
                int y = listTop + i * rowHeight;
                int rowLeft = panelLeft + 12;
                int rowRight = panelRight - 12;
                drawRect(rowLeft, y, rowRight, y + 40, 0x9914171E);
                drawRect(rowLeft, y, rowRight, y + 1, 0x554B515D);
                drawString(fontRendererObj, entry.getName(), rowLeft + 10, y + 5, 0xFFF3F4F6);
                drawString(fontRendererObj, "Last visited", rowLeft + 10, y + 19, 0xFFFFD866);
                drawString(fontRendererObj, entry.getAddress(), rowLeft + 84, y + 19, 0xFFB8BDC8);
                drawString(fontRendererObj, formatLastVisited(entry.getLastVisited()),
                        rowLeft + 10, y + 30, 0xFF8E95A3);
                drawString(fontRendererObj, "JOIN COUNT: " + entry.getJoinCount(),
                        panelRight - 205, y + 30, 0xFFFFD866);
            }
        }

        drawString(fontRendererObj, entries.size() + " / "
                + ServerHistoryManager.MAX_SAVED_SERVERS + " SERVERS SAVED",
                panelLeft + 12, panelBottom - 48, 0xFFB8BDC8);
        if (entries.size() > visibleRows) {
            drawString(fontRendererObj, "Scroll to view all saved servers",
                    panelRight - 190, panelBottom - 48, 0xFF8E95A3);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String formatLastVisited(long timestamp) {
        return new SimpleDateFormat("dd MMM · HH:mm", Locale.ENGLISH).format(new Date(timestamp));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == CLEAR_HISTORY) {
            ServerHistoryManager.clearHistory();
            scrollOffset = 0;
            initGui();
        } else if (button.id == SETTINGS) {
            mc.displayGuiScreen(new ServerHistorySettingsGui(this));
        } else if (button.id >= JOIN_BASE && button.id < JOIN_BASE + visibleEntries.size()) {
            ServerHistoryManager.Entry entry = visibleEntries.get(button.id - JOIN_BASE);
            ServerData server = new ServerData(entry.getName(), entry.getAddress(), false);
            mc.displayGuiScreen(new GuiConnecting(this, mc, server));
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int maxScroll = Math.max(0, ServerHistoryManager.getEntries().size() - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
