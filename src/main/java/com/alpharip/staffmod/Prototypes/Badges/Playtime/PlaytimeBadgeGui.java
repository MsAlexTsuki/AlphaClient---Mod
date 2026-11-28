package com.alpharip.staffmod.badges.playtime;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Minecraft 1.8.9 preview for cumulative playtime badges. */
public final class PlaytimeBadgeGui extends GuiScreen {
    private static final int BACK_BUTTON = 1;
    private final GuiScreen parent;

    public PlaytimeBadgeGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(BACK_BUTTON, width / 2 - 40, height - 29, 80, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(390, Math.max(280, width - 24));
        int panelHeight = Math.min(250, height - 42);
        int left = (width - panelWidth) / 2;
        int top = Math.max(8, (height - panelHeight) / 2 - 5);
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        drawRect(left, top, right, bottom, 0xF00B0D12);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "PLAYTIME BADGES", width / 2, top + 12, 0xFFF2F3F5);

        long totalSeconds = PlaytimeBadgeService.getTotalPlaytimeSeconds();
        drawCenteredString(fontRendererObj, "Total Minecraft playtime: " + formatDuration(totalSeconds),
                width / 2, top + 30, 0xFFB8BDC8);

        List<PlaytimeBadgeStatus> statuses = PlaytimeBadgeService.getCurrentBadges();
        int rowTop = top + 51;
        for (int i = 0; i < statuses.size(); i++) {
            PlaytimeBadgeStatus status = statuses.get(i);
            int y = rowTop + i * 19;
            if (y + 16 > bottom - 8) break;

            String state = status.isEarned() ? "UNLOCKED" : status.getProgressPercent() + "%";
            int stateColor = status.isEarned() ? 0xFFFFD866 : 0xFF858D9B;
            drawString(fontRendererObj, status.getTier().getDisplayName(), left + 18, y, 0xFFE0E3E8);
            drawString(fontRendererObj, state, right - fontRendererObj.getStringWidth(state) - 18, y, stateColor);

            if (!status.isEarned()) {
                int barLeft = left + 120;
                int barRight = right - 86;
                int barY = y + 9;
                drawRect(barLeft, barY, barRight, barY + 3, 0xFF30343D);
                drawRect(barLeft, barY, barLeft + Math.max(1,
                        (barRight - barLeft) * status.getProgressPercent() / 100), barY + 3, 0xFFFFD866);
            }
        }

        drawCenteredString(fontRendererObj, "Uses the existing cumulative Playtime Tracker; HUD can stay hidden.",
                width / 2, bottom - 17, 0xFF858D9B);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static String formatDuration(long totalSeconds) {
        long seconds = Math.max(0L, totalSeconds);
        long days = seconds / 86400L;
        seconds %= 86400L;
        long hours = seconds / 3600L;
        seconds %= 3600L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}