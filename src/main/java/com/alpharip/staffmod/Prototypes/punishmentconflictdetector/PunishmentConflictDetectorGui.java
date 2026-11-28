package com.alpharip.staffmod.punishmentconflictdetector;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/** Read-only viewer for conflicts found in the locally supplied punishment records. */
public final class PunishmentConflictDetectorGui extends GuiScreen {
    private static final int REFRESH = 1;
    private static final int CLOSE = 2;
    private static final int ACCENT = 0xFFFFC83D;

    private final GuiScreen parent;
    private final PunishmentConflictDetector detector;
    private List<PunishmentConflict> conflicts;

    public PunishmentConflictDetectorGui(GuiScreen parent, PunishmentConflictDetector detector) {
        if (detector == null) throw new IllegalArgumentException("Detector must not be null.");
        this.parent = parent;
        this.detector = detector;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(REFRESH, width / 2 - 104, height - 34, 100, 20, "REFRESH"));
        buttonList.add(new GuiButton(CLOSE, width / 2 + 4, height - 34, 100, 20, "CLOSE"));
        refresh();
    }

    private void refresh() {
        conflicts = detector.detectConflicts();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == REFRESH) {
            refresh();
        } else if (button.id == CLOSE) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "PUNISHMENT CONFLICT DETECTOR", width / 2, 18, ACCENT);
        drawCenteredString(fontRendererObj, "Same target + same punishment type + active at the current time",
                width / 2, 34, 0xFF9AA3B2);

        if (conflicts == null || conflicts.isEmpty()) {
            drawCenteredString(fontRendererObj, "No active conflicts found in the supplied records.",
                    width / 2, height / 2 - 4, 0xFFE5E7EB);
        } else {
            int y = 62;
            int maxRows = Math.max(0, (height - 112) / 42);
            int shown = Math.min(conflicts.size(), maxRows);
            for (int i = 0; i < shown; i++) {
                PunishmentConflict conflict = conflicts.get(i);
                PunishmentRecord first = conflict.getFirst();
                PunishmentRecord second = conflict.getSecond();
                drawRect(24, y - 5, width - 24, y + 34, 0xA00B0D12);
                drawString(fontRendererObj, conflict.getSummary(), 34, y, 0xFFFF7777);
                drawString(fontRendererObj, "Records: " + first.getRecordId() + " / " + second.getRecordId(),
                        34, y + 12, 0xFFE5E7EB);
                drawString(fontRendererObj, "Reasons: " + shortText(first.getReason()) + " | " + shortText(second.getReason()),
                        34, y + 24, 0xFF9AA3B2);
                y += 42;
            }
            if (shown < conflicts.size()) {
                drawCenteredString(fontRendererObj, (conflicts.size() - shown) + " more conflict(s) not shown",
                        width / 2, height - 52, 0xFF9AA3B2);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String shortText(String value) {
        if (value == null || value.isEmpty()) return "(no reason)";
        return value.length() <= 32 ? value : value.substring(0, 29) + "...";
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
