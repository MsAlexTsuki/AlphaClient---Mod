package com.alpharip.staffdashboardexpansion.shifts;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Standalone Minecraft 1.8.9 shift planner.
 * Date fields use yyyy-MM-dd HH:mm (local client time).
 * TODO: Register/open from Staff Dashboard and apply staff permissions.
 */
public final class StaffShiftBoardGui extends GuiScreen {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final GuiScreen parent;
    private final StaffShiftManager manager;
    private GuiTextField staffField, titleField, startField, endField;
    private int scrollOffset, selectedIndex = -1;
    private String feedback = "";

    public StaffShiftBoardGui(GuiScreen parent, StaffShiftManager manager) {
        if (manager == null) throw new IllegalArgumentException("manager must not be null.");
        this.parent = parent;
        this.manager = manager;
    }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        int x = width / 2 - 100;
        staffField = new GuiTextField(0, fontRendererObj, x, 31, 200, 18);
        titleField = new GuiTextField(1, fontRendererObj, x, 54, 200, 18);
        startField = new GuiTextField(2, fontRendererObj, x, 77, 200, 18);
        endField = new GuiTextField(3, fontRendererObj, x, 100, 200, 18);
        staffField.setMaxStringLength(32); titleField.setMaxStringLength(40);
        startField.setMaxStringLength(16); endField.setMaxStringLength(16);
        buttonList.add(new GuiButton(0, x, 123, 98, 20, "Add Shift"));
        buttonList.add(new GuiButton(1, x + 102, 123, 98, 20, "Delete Selected"));
        buttonList.add(new GuiButton(2, x, height - 28, 98, 20, "Refresh"));
        buttonList.add(new GuiButton(3, x + 102, height - 28, 98, 20, "Back"));
        refreshRows();
    }

    private List<StaffShift> shifts() { return manager.getBoard().getShifts(); }
    private void refreshRows() {
        while (buttonList.size() > 4) buttonList.remove(buttonList.size() - 1);
        List<StaffShift> shifts = shifts();
        int visible = Math.max(0, Math.min(8, (height - 185) / 19));
        for (int i = scrollOffset; i < shifts.size() && i < scrollOffset + visible; i++) {
            StaffShift s = shifts.get(i);
            String label = s.getStaffName() + " | " + s.getTitle() + " | "
                    + s.getStart().format(DATE_TIME) + " | " + s.getStatus().getLabel();
            buttonList.add(new GuiButton(20 + i - scrollOffset, width / 2 - 145,
                    151 + (i - scrollOffset) * 19, 290, 18, label));
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        try {
            if (button.id == 0) {
                LocalDateTime start = LocalDateTime.parse(startField.getText().trim(), DATE_TIME);
                LocalDateTime end = LocalDateTime.parse(endField.getText().trim(), DATE_TIME);
                StaffShift added = manager.schedule(staffField.getText(), titleField.getText(), start, end);
                feedback = "Scheduled shift #" + added.getId();
                scrollOffset = Math.max(0, shifts().size() - 1);
                selectedIndex = shifts().size() - 1;
                refreshRows();
            } else if (button.id == 1) {
                if (selectedIndex >= 0 && selectedIndex < shifts().size()) {
                    manager.getBoard().removeShift(shifts().get(selectedIndex).getId());
                    selectedIndex = -1; feedback = "Shift deleted."; refreshRows();
                } else feedback = "Select a shift first.";
            } else if (button.id == 2) {
                refreshRows(); feedback = "Shift list refreshed.";
            } else if (button.id == 3) mc.displayGuiScreen(parent);
            else if (button.id >= 20) {
                int index = scrollOffset + button.id - 20;
                if (index < shifts().size()) {
                    selectedIndex = index;
                    manager.cycleStatus(shifts().get(index).getId());
                    feedback = "Selected shift status updated.";
                    refreshRows();
                }
            }
        } catch (DateTimeParseException ex) {
            feedback = "Use yyyy-MM-dd HH:mm for both date fields.";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            feedback = ex.getMessage();
        }
    }

    @Override protected void keyTyped(char c, int key) throws IOException {
        if (staffField.textboxKeyTyped(c,key) || titleField.textboxKeyTyped(c,key)
                || startField.textboxKeyTyped(c,key) || endField.textboxKeyTyped(c,key)) return;
        if (key == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent); else super.keyTyped(c,key);
    }
    @Override protected void mouseClicked(int x,int y,int button) throws IOException {
        super.mouseClicked(x,y,button);
        staffField.mouseClicked(x,y,button); titleField.mouseClicked(x,y,button);
        startField.mouseClicked(x,y,button); endField.mouseClicked(x,y,button);
    }
    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int visible = Math.max(1, Math.min(8, (height - 185) / 19));
            scrollOffset = Math.max(0, Math.min(Math.max(0, shifts().size() - visible),
                    scrollOffset + (wheel > 0 ? -1 : 1)));
            refreshRows();
        }
    }
    @Override public void updateScreen() {
        staffField.updateCursorCounter(); titleField.updateCursorCounter();
        startField.updateCursorCounter(); endField.updateCursorCounter();
    }
    @Override public void drawScreen(int mx,int my,float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Shift Board", width / 2, 10, 0xFFFFFF);
        drawString(fontRendererObj, "Staff member", width / 2 - 100, 21, 0xAAAAAA); staffField.drawTextBox();
        drawString(fontRendererObj, "Shift title", width / 2 - 100, 44, 0xAAAAAA); titleField.drawTextBox();
        drawString(fontRendererObj, "Start (yyyy-MM-dd HH:mm)", width / 2 - 100, 67, 0xAAAAAA); startField.drawTextBox();
        drawString(fontRendererObj, "End (yyyy-MM-dd HH:mm)", width / 2 - 100, 90, 0xAAAAAA); endField.drawTextBox();
        drawString(fontRendererObj, feedback, 8, height - 42, 0xDDDDDD);
        super.drawScreen(mx,my,partialTicks);
    }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    @Override public boolean doesGuiPauseGame() { return false; }
}
