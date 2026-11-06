package com.alpharip.staffmod.staffdashboardexpansion.staffqueue;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

/**
 * Lightweight Minecraft 1.8.9 screen prototype for reviewing and claiming tasks.
 * Opening this screen is intentionally left to the host module until permissions
 * and the staff identity source are connected.
 */
public final class StaffQueueGui extends GuiScreen {
    private final StaffQueue queue;
    private final String currentStaffName;
    private int selectedIndex;
    private List<StaffQueueEntry> visibleTasks;

    public StaffQueueGui(StaffQueue queue, String currentStaffName) {
        if (queue == null) {
            throw new IllegalArgumentException("queue must not be null.");
        }
        if (currentStaffName == null || currentStaffName.trim().isEmpty()) {
            throw new IllegalArgumentException("currentStaffName must not be blank.");
        }
        this.queue = queue;
        this.currentStaffName = currentStaffName.trim();
        this.visibleTasks = queue.getTasks();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        buttonList.add(new GuiButton(0, center - 154, height -  thirty(), 100, 20, "Claim"));
        buttonList.add(new GuiButton(1, center - 50, height -  thirty(), 100, 20, "Complete"));
        buttonList.add(new GuiButton(2, center + 54, height -  thirty(), 100, 20, "Refresh"));
        refreshTasks();
    }

    private void refreshTasks() {
        visibleTasks = queue.getTasks();
        if (selectedIndex >= visibleTasks.size()) {
            selectedIndex = Math.max(0, visibleTasks.size() - 1);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        StaffQueueEntry selected = getSelectedTask();
        if (button.id == 0 && selected != null) {
            queue.claim(selected.getId(), currentStaffName);
        } else if (button.id == 1 && selected != null) {
            queue.complete(selected.getId());
        }
        refreshTasks();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }
        int top = 42;
        int rowHeight = 28;
        for (int i = 0; i < visibleTasks.size(); i++) {
            int y = top + i * rowHeight;
            if (mouseY >= y && mouseY < y + rowHeight) {
                selectedIndex = i;
                break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Queue", width / 2, 14, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
            "Open tasks: " + queue.getOpenTaskCount(), width / 2, 27, 0xAAAAAA);

        int top = 42;
        int rowHeight = 28;
        int maxRows = Math.max(0, (height - 82) / rowHeight);
        for (int i = 0; i < visibleTasks.size() && i < maxRows; i++) {
            StaffQueueEntry task = visibleTasks.get(i);
            int y = top + i * rowHeight;
            int background = i == selectedIndex ? 0xAA665522 : 0x88303030;
            drawRect(width / 2 - 190, y, width / 2 + 190, y + 25, background);
            String line = "[" + task.getPriority().name() + "] "
                + task.getTitle() + " - " + task.getStatus().name();
            drawString(fontRendererObj, trim(line, 54), width / 2 - 182, y + 4, 0xFFFFFF);
            String assignee = task.getAssignedTo().isEmpty()
                ? "Unassigned" : "Assigned: " + task.getAssignedTo();
            drawString(fontRendererObj, trim(assignee, 48),
                width / 2 - 182, y + 15, 0xAAAAAA);
        }

        if (visibleTasks.isEmpty()) {
            drawCenteredString(fontRendererObj, "No staff tasks in the queue.",
                width / 2, height / 2, 0xAAAAAA);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private StaffQueueEntry getSelectedTask() {
        return selectedIndex >= 0 && selectedIndex < visibleTasks.size()
            ? visibleTasks.get(selectedIndex) : null;
    }

    private String trim(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
