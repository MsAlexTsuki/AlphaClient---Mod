package com.alpharip.staffdashboardexpansion.availability;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

/**
 * Standalone Minecraft 1.8.9 screen for the local availability board.
 * TODO: Register/open this screen from Staff Dashboard after integration.
 */
public final class StaffAvailabilityGui extends GuiScreen {
    private final GuiScreen parent;
    private final StaffAvailabilityBoard board;
    private GuiTextField nameField;
    private GuiTextField noteField;
    private int filterIndex;
    private int scrollOffset;

    public StaffAvailabilityGui(GuiScreen parent, StaffAvailabilityBoard board) {
        this.parent = parent;
        this.board = board;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        nameField = new GuiTextField(0, fontRendererObj, width / 2 - 100, 34, 200, 18);
        nameField.setMaxStringLength(16);
        noteField = new GuiTextField(1, fontRendererObj, width / 2 - 100, 57, 200, 18);
        noteField.setMaxStringLength(48);
        buttonList.add(new GuiButton(0, width / 2 - 100, 80, 98, 20, "Set Available"));
        buttonList.add(new GuiButton(1, width / 2 + 2, 80, 98, 20, "Set Busy"));
        buttonList.add(new GuiButton(2, width / 2 - 100, 104, 98, 20, "Set Away"));
        buttonList.add(new GuiButton(3, width / 2 + 2, 104, 98, 20, "Set Unavailable"));
        buttonList.add(new GuiButton(4, width / 2 - 100, height - 28, 98, 20, "Filter: " + filterName()));
        buttonList.add(new GuiButton(5, width / 2 + 2, height - 28, 98, 20, "Back"));
        refreshRows();
    }

    private void refreshRows() {
        while (buttonList.size() > 6) buttonList.remove(buttonList.size() - 1);
        List<StaffAvailabilityEntry> entries = board.filter(filterStatus());
        int visible = Math.max(0, Math.min(8, (height - 170) / 22));
        for (int i = scrollOffset; i < entries.size() && i < scrollOffset + visible; i++) {
            StaffAvailabilityEntry entry = entries.get(i);
            int row = i - scrollOffset;
            buttonList.add(new GuiButton(20 + row, width / 2 - 100, 135 + row * 22, 200, 20,
                    entry.getStaffName() + ": " + entry.getStatus().getDisplayName()));
        }
    }

    private StaffAvailabilityStatus filterStatus() {
        return filterIndex == 0 ? null : StaffAvailabilityStatus.values()[filterIndex - 1];
    }
    private String filterName() { return filterStatus() == null ? "All" : filterStatus().getDisplayName(); }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        if (button.id >= 0 && button.id <= 3) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                StaffAvailabilityStatus status = StaffAvailabilityStatus.values()[button.id];
                board.setAvailability(name, status, noteField.getText());
                refreshRows();
            }
        } else if (button.id == 4) {
            filterIndex = (filterIndex + 1) % (StaffAvailabilityStatus.values().length + 1);
            scrollOffset = 0;
            button.displayString = "Filter: " + filterName();
            refreshRows();
        } else if (button.id == 5) {
            mc.displayGuiScreen(parent);
        } else if (button.id >= 20) {
            List<StaffAvailabilityEntry> entries = board.filter(filterStatus());
            int index = scrollOffset + button.id - 20;
            if (index < entries.size()) {
                StaffAvailabilityEntry entry = entries.get(index);
                board.updateStatus(entry.getStaffName(), entry.getStatus().next());
                refreshRows();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField.textboxKeyTyped(typedChar, keyCode)) return;
        if (noteField.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        noteField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int max = Math.max(0, board.filter(filterStatus()).size() - Math.max(1, Math.min(8, (height - 170) / 22)));
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel > 0 ? -1 : 1)));
            refreshRows();
        }
    }

    @Override
    public void updateScreen() {
        nameField.updateCursorCounter();
        noteField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Availability", width / 2, 12, 0xFFFFFF);
        nameField.drawTextBox();
        noteField.drawTextBox();
        drawString(fontRendererObj, "Staff name", width / 2 - 100, 23, 0xAAAAAA);
        drawString(fontRendererObj, "Optional note", width / 2 - 100, 46, 0xAAAAAA);
        drawString(fontRendererObj, "Available: " + board.count(StaffAvailabilityStatus.AVAILABLE)
                + "  Busy: " + board.count(StaffAvailabilityStatus.BUSY)
                + "  Away: " + board.count(StaffAvailabilityStatus.AWAY)
                + "  Unavailable: " + board.count(StaffAvailabilityStatus.UNAVAILABLE),
                8, height - 48, 0xDDDDDD);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
