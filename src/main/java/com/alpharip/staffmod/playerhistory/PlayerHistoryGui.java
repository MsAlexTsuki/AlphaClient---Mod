package com.alpharip.staffmod.playerhistory;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Standalone searchable overview; it is not attached to an existing menu. */
public final class PlayerHistoryGui extends GuiScreen {
    private static final int BACK_BUTTON = 1;

    private final GuiScreen parent;
    private final PlayerHistory history;
    private GuiTextField searchField;
    private int scrollOffset;
    private List<PlayerHistoryEntry> visibleEntries = new ArrayList<PlayerHistoryEntry>();
    private int listTop;
    private int listBottom;

    public PlayerHistoryGui(GuiScreen parent, PlayerHistory history) {
        this.parent = parent;
        this.history = history;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = Math.min(560, Math.max(300, width - 24));
        int left = (width - panelWidth) / 2;
        int top = 24;
        searchField = new GuiTextField(2, fontRendererObj, left + 14, top + 38,
                panelWidth - 28, 20);
        searchField.setMaxStringLength(32);
        listTop = top + 88;
        listBottom = height - 48;
        buttonList.add(new GuiButton(BACK_BUTTON, left + 14, height - 34, 78, 20, "BACK"));
        refreshEntries();
    }

    @Override
    public void updateScreen() {
        if (searchField != null) searchField.updateCursorCounter();
        refreshEntries();
    }

    private void refreshEntries() {
        visibleEntries.clear();
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        for (PlayerHistoryEntry entry : history.getStore().getEntriesSnapshot()) {
            if (query.isEmpty() || entry.getPlayerName().toLowerCase(Locale.ROOT).contains(query)
                    || entry.getPlayerId().toString().toLowerCase(Locale.ROOT).contains(query)) {
                visibleEntries.add(entry);
            }
        }
        int maxRows = Math.max(0, (listBottom - listTop) / 28);
        int maxOffset = Math.max(0, visibleEntries.size() - maxRows);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(560, Math.max(300, width - 24));
        int left = (width - panelWidth) / 2;
        int top = 24;
        drawRect(left, top, left + panelWidth, height - 24, 0xF00A0C11);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "PLAYER HISTORY", width / 2, top + 12, 0xFFF3F4F6);
        drawString(fontRendererObj, "SEARCH PLAYER", left + 14, top + 27, 0xFF9AA3B2);
        searchField.drawTextBox();

        drawString(fontRendererObj, "PLAYER", left + 16, top + 70, 0xFFFFC83D);
        drawString(fontRendererObj, "SESSIONS", left + panelWidth - 205, top + 70, 0xFFFFC83D);
        drawString(fontRendererObj, "TOTAL TIME", left + panelWidth - 135, top + 70, 0xFFFFC83D);

        int rowY = listTop;
        int maxRows = Math.max(0, (listBottom - listTop) / 28);
        long now = System.currentTimeMillis();
        for (int i = scrollOffset; i < visibleEntries.size()
                && i < scrollOffset + maxRows; i++) {
            PlayerHistoryEntry entry = visibleEntries.get(i);
            drawRect(left + 10, rowY - 3, left + panelWidth - 10, rowY + 22,
                    (i % 2 == 0) ? 0x66303540 : 0x4420252D);
            int statusColor = entry.isOnline() ? 0xFF75D69C : 0xFF9AA3B2;
            String status = entry.isOnline() ? "ONLINE" : "OFFLINE";
            drawString(fontRendererObj, entry.getPlayerName(), left + 16, rowY + 2, 0xFFF3F4F6);
            drawString(fontRendererObj, status, left + 16, rowY + 12, statusColor);
            drawString(fontRendererObj, String.valueOf(entry.getSessionCount()),
                    left + panelWidth - 205, rowY + 4, 0xFFE2E5EB);
            drawString(fontRendererObj,
                    PlayerHistoryFormatter.duration(entry.getTotalOnlineMillisAt(now)),
                    left + panelWidth - 135, rowY + 4, 0xFFE2E5EB);
            rowY += 28;
        }

        if (visibleEntries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No player history found.",
                    width / 2, listTop + 16, 0xFF9AA3B2);
        }
        drawString(fontRendererObj, "Local client observations only",
                left + 14, height - 43, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset += wheel > 0 ? -3 : 3;
            refreshEntries();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField != null && searchField.textboxKeyTyped(typedChar, keyCode)) {
            scrollOffset = 0;
            refreshEntries();
            return;
        }
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (searchField != null) searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK_BUTTON) mc.displayGuiScreen(parent);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
