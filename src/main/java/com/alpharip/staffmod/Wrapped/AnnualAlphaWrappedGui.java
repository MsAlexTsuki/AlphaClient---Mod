package com.alpharip.staffmod.wrapped;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Local preview GUI for annual Alpha Wrapped statistics (Minecraft 1.8.9). */
public final class AnnualAlphaWrappedGui extends GuiScreen {
    private static final int BACK_BUTTON = 1;
    private final GuiScreen parent;
    private final AnnualAlphaWrappedSummary summary;

    public AnnualAlphaWrappedGui(GuiScreen parent, AnnualAlphaWrappedSummary summary) {
        this.parent = parent;
        this.summary = summary;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(BACK_BUTTON, width / 2 - 42, height - 30, 84, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK_BUTTON) mc.displayGuiScreen(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(420, width - 24);
        int left = (width - panelWidth) / 2;
        int top = Math.max(12, (height - 220) / 2);
        int innerWidth = panelWidth - 28;
        drawRect(left, top, left + panelWidth, top + 205, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ALPHA WRAPPED", width / 2, top + 13, 0xFFF2F3F5);

        if (summary == null || !summary.isAvailable()) {
            drawCenteredString(fontRendererObj, "No activity data available for this year.",
                    width / 2, top + 82, 0xFF9DA5B3);
        } else {
            drawCenteredString(fontRendererObj, Integer.toString(summary.getYear()),
                    width / 2, top + 34, 0xFFFFD866);
            int gap = 8;
            int cardWidth = (innerWidth - gap) / 2;
            int cardHeight = 55;
            int x1 = left + 14;
            int x2 = x1 + cardWidth + gap;
            int y1 = top + 58;
            int y2 = y1 + cardHeight + 9;
            AnnualAlphaWrappedCard.draw(mc, x1, y1, cardWidth, cardHeight, "TOTAL PLAYTIME",
                    formatDuration(summary.getTotalPlaytimeMillis()), 0xFFFFD866);
            AnnualAlphaWrappedCard.draw(mc, x2, y1, cardWidth, cardHeight, "SESSIONS",
                    Long.toString(summary.getSessions()), 0xFF87CEFA);
            AnnualAlphaWrappedCard.draw(mc, x1, y2, cardWidth, cardHeight, "SERVERS VISITED",
                    Long.toString(summary.getServersVisited()), 0xFFB58CFF);
            AnnualAlphaWrappedCard.draw(mc, x2, y2, cardWidth, cardHeight, "YEAR",
                    Integer.toString(summary.getYear()), 0xFFFFD866);
        }
        drawCenteredString(fontRendererObj, "Preview uses supplied local session samples only.",
                width / 2, top + 184, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String formatDuration(long millis) {
        long seconds = Math.max(0L, millis / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        return hours + "h " + minutes + "m";
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
