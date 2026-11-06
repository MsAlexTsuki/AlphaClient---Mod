package com.alpharip.staffdashboardexpansion.staffdutymode;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Minecraft 1.8.9 screen prototype for changing and viewing local duty state.
 *
 * Opening this screen remains the responsibility of the host module. This GUI
 * does not authenticate staff or publish duty changes to a server.
 */
public final class StaffDutyModeGui extends GuiScreen {
    private final StaffDutyManager manager;

    public StaffDutyModeGui(StaffDutyManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("manager must not be null.");
        }
        this.manager = manager;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int buttonY = height - 42;
        buttonList.add(new GuiButton(0, center - 154, buttonY, 100, 20, "On Duty"));
        buttonList.add(new GuiButton(1, center - 50, buttonY, 100, 20, "Busy"));
        buttonList.add(new GuiButton(2, center + 54, buttonY, 100, 20, "Off Duty"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            manager.goOnDuty();
        } else if (button.id == 1) {
            manager.setBusy();
        } else if (button.id == 2) {
            manager.goOffDuty();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Duty Mode", width / 2, 18, 0xFFFFFF);

        StaffDutyMode mode = manager.getCurrentMode();
        StaffDutyState state = mode.getState();
        int panelLeft = width / 2 - 150;
        int panelTop = height / 2 - 58;
        int panelRight = width / 2 + 150;
        int panelBottom = height / 2 + 58;

        drawRect(panelLeft, panelTop, panelRight, panelBottom, 0xAA202020);
        drawCenteredString(fontRendererObj, mode.getStaffName(),
            width / 2, panelTop + 15, 0xFFDDDDDD);
        drawCenteredString(fontRendererObj, state.getDisplayName(),
            width / 2, panelTop + 39, state.getColor());
        drawCenteredString(fontRendererObj, "State duration: " + formatDuration(mode.getStateDurationMillis()),
            width / 2, panelTop + 61, 0xFFBBBBBB);

        String assignmentStatus = manager.canReceiveAssignments()
            ? "Available for assignments"
            : state == StaffDutyState.BUSY ? "Not available for assignments" : "Not on duty";
        drawCenteredString(fontRendererObj, assignmentStatus,
            width / 2, panelTop + 79, 0xFFAAAAAA);

        drawCenteredString(fontRendererObj, "Local prototype only - not server-synchronized",
            width / 2, height - 65, 0xFFFFC84A);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String formatDuration(long durationMillis) {
        long totalSeconds = durationMillis / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
