package com.alpharip.staffmod.trackers.acknowledgement;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Minecraft 1.8.9 GUI preview for reviewing and acknowledging local records. */
public final class AcknowledgementGui extends GuiScreen {
    private final AcknowledgementService service;
    private final String currentStaffName;
    private List<AcknowledgementEntry> visibleEntries;
    private int selectedIndex;

    public AcknowledgementGui(AcknowledgementService service, String currentStaffName) {
        if (service == null || currentStaffName == null || currentStaffName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service and current staff name are required.");
        }
        this.service = service;
        this.currentStaffName = currentStaffName.trim();
        this.visibleEntries = service.getVisibleEntries("");
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int y = height - 30;
        buttonList.add(new GuiButton(0, center - 104, y, 100, 20, "Acknowledge"));
        buttonList.add(new GuiButton(1, center + 4, y, 100, 20, "Refresh"));
        refreshEntries();
    }

    private void refreshEntries() {
        visibleEntries = service.getVisibleEntries("");
        if (selectedIndex >= visibleEntries.size()) {
            selectedIndex = Math.max(0, visibleEntries.size() - 1);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        AcknowledgementEntry selected = getSelectedEntry();
        if (button.id == 0 && selected != null) {
            service.acknowledge(selected.getId(), currentStaffName);
        }
        refreshEntries();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }
        int top = 48;
        int rowHeight = 42;
        int maxRows = Math.max(0, (height - 86) / rowHeight);
        for (int i = 0; i < visibleEntries.size() && i < maxRows; i++) {
            int rowY = top + i * rowHeight;
            if (mouseY >= rowY && mouseY < rowY + rowHeight) {
                selectedIndex = i;
                break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Acknowledgement Tracker", width / 2, 14, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
            "Pending: " + service.getRegistry().getPendingCount(), width / 2, 29, 0xAAAAAA);

        int top = 48;
        int rowHeight = 42;
        int maxRows = Math.max(0, (height - 86) / rowHeight);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM HH:mm", Locale.ROOT);

        for (int i = 0; i < visibleEntries.size() && i < maxRows; i++) {
            AcknowledgementEntry entry = visibleEntries.get(i);
            int rowY = top + i * rowHeight;
            drawRect(width / 2 - 190, rowY, width / 2 + 190, rowY + 38,
                i == selectedIndex ? 0xAA665522 : 0x88303030);
            drawString(fontRendererObj, trim(entry.getSubject(), 58),
                width / 2 - 182, rowY + 4, 0xFFFFFF);
            drawString(fontRendererObj,
                entry.getStatus().name() + " | by " + entry.getCreatedBy(),
                width / 2 - 182, rowY + 17, 0xCCCCCC);
            drawString(fontRendererObj,
                dateFormat.format(new Date(entry.getCreatedAtMillis())),
                width / 2 - 182, rowY + 28, 0xAAAAAA);
        }

        if (visibleEntries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No acknowledgement records.",
                width / 2, height / 2, 0xAAAAAA);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private AcknowledgementEntry getSelectedEntry() {
        return selectedIndex >= 0 && selectedIndex < visibleEntries.size()
            ? visibleEntries.get(selectedIndex) : null;
    }

    private String trim(String value, int maxLength) {
        return value.length() <= maxLength
            ? value : value.substring(0, maxLength - 3) + "...";
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
