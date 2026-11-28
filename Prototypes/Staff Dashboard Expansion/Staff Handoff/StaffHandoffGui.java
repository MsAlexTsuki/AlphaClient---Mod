package com.alpharip.staffmod.staffdashboardexpansion.staffhandoff;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Minecraft 1.8.9 screen prototype for reviewing and responding to handoffs. */
public final class StaffHandoffGui extends GuiScreen {
    private final StaffHandoffManager manager;
    private final String currentStaffName;
    private List<StaffHandoff> visibleHandoffs;
    private int selectedIndex;

    public StaffHandoffGui(StaffHandoffManager manager, String currentStaffName) {
        if (manager == null) {
            throw new IllegalArgumentException("manager must not be null.");
        }
        if (currentStaffName == null || currentStaffName.trim().isEmpty()) {
            throw new IllegalArgumentException("currentStaffName must not be blank.");
        }
        this.manager = manager;
        this.currentStaffName = currentStaffName.trim();
        this.visibleHandoffs = manager.getAll();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int y = height - 30;
        buttonList.add(new GuiButton(0, center - 156, y, 100, 20, "Accept"));
        buttonList.add(new GuiButton(1, center - 52, y, 100, 20, "Reject"));
        buttonList.add(new GuiButton(2, center + 52, y, 100, 20, "Refresh"));
        refreshHandoffs();
    }

    private void refreshHandoffs() {
        visibleHandoffs = manager.getAll();
        if (selectedIndex >= visibleHandoffs.size()) {
            selectedIndex = Math.max(0, visibleHandoffs.size() - 1);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        StaffHandoff selected = getSelectedHandoff();
        if (selected != null && button.id == 0) {
            manager.accept(selected.getId(), currentStaffName);
        } else if (selected != null && button.id == 1) {
            manager.reject(selected.getId(), currentStaffName);
        }
        refreshHandoffs();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }
        int top = 45;
        int rowHeight = 43;
        for (int i = 0; i < visibleHandoffs.size(); i++) {
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
        drawCenteredString(fontRendererObj, "Staff Handoff", width / 2, 14, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
            "Pending handoffs: " + manager.getPendingCount(), width / 2, 28, 0xAAAAAA);

        int top = 45;
        int rowHeight = 43;
        int maxRows = Math.max(0, (height - 85) / rowHeight);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM HH:mm", Locale.ROOT);

        for (int i = 0; i < visibleHandoffs.size() && i < maxRows; i++) {
            StaffHandoff handoff = visibleHandoffs.get(i);
            int rowY = top + i * rowHeight;
            int background = i == selectedIndex ? 0xAA665522 : 0x88303030;
            drawRect(width / 2 - 195, rowY, width / 2 + 195, rowY + 39, background);

            String route = handoff.getFromStaff() + " -> " + handoff.getToStaff();
            drawString(fontRendererObj, trim(route, 62),
                width / 2 - 187, rowY + 4, 0xFFFFFF);
            drawString(fontRendererObj, trim(handoff.getSummary(), 70),
                width / 2 - 187, rowY + 16, 0xCCCCCC);

            String metadata = handoff.getStatus().name() + " | "
                + dateFormat.format(new Date(handoff.getCreatedAtMillis()));
            drawString(fontRendererObj, metadata,
                width / 2 - 187, rowY + 28, 0xAAAAAA);
        }

        if (visibleHandoffs.isEmpty()) {
            drawCenteredString(fontRendererObj, "No handoffs have been created.",
                width / 2, height / 2, 0xAAAAAA);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private StaffHandoff getSelectedHandoff() {
        return selectedIndex >= 0 && selectedIndex < visibleHandoffs.size()
            ? visibleHandoffs.get(selectedIndex) : null;
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
