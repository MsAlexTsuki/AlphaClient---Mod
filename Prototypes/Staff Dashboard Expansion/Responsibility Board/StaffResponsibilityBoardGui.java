package com.alpharip.staffmod.staffdashboard.responsibility;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/**
 * Standalone Minecraft 1.8.9 GUI prototype.
 * Host dashboard opening and permission enforcement are intentionally not wired.
 */
public class StaffResponsibilityBoardGui extends GuiScreen {
    private final GuiScreen parent;
    private final StaffResponsibilityBoard board;
    private GuiTextField titleField, descriptionField, assigneeField;
    private int selectedIndex;
    private int listOffset;
    private int filterMode;
    private String feedback = "";

    public StaffResponsibilityBoardGui(GuiScreen parent, StaffResponsibilityBoard board) {
        this.parent = parent;
        this.board = board == null ? new StaffResponsibilityBoard() : board;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = width / 2 - 150;
        titleField = new GuiTextField(0, fontRendererObj, left, 48, 145, 18);
        descriptionField = new GuiTextField(1, fontRendererObj, left, 76, 145, 18);
        assigneeField = new GuiTextField(2, fontRendererObj, left, 104, 145, 18);
        titleField.setMaxStringLength(40);
        descriptionField.setMaxStringLength(90);
        assigneeField.setMaxStringLength(32);
        buttonList.add(new GuiButton(0, left, 130, 145, 20, "Create Responsibility"));
        buttonList.add(new GuiButton(1, left, 158, 70, 20, "Priority"));
        buttonList.add(new GuiButton(2, left + 75, 158, 70, 20, "Assign"));
        buttonList.add(new GuiButton(3, left, 186, 145, 20, "Cycle Status"));
        buttonList.add(new GuiButton(4, left, 214, 145, 20, "Delete Selected"));
        buttonList.add(new GuiButton(5, width / 2 + 10, height - 48, 145, 20, "Filter: All"));
        buttonList.add(new GuiButton(6, width / 2 - 150, height - 48, 145, 20, "Back"));
        buttonList.add(new GuiButton(7, width / 2 - 75, 242, 70, 20, "Prev"));
        buttonList.add(new GuiButton(8, width / 2, 242, 70, 20, "Next"));
        clampSelection();
    }

    private List<StaffResponsibility> visibleItems() {
        if (filterMode == 1) return board.getManager().getByStatus(StaffResponsibilityStatus.UNASSIGNED);
        if (filterMode == 2) return board.getManager().getByStatus(StaffResponsibilityStatus.IN_PROGRESS);
        if (filterMode == 3) return board.getManager().getByStatus(StaffResponsibilityStatus.COMPLETED);
        return board.getManager().getAll();
    }

    private StaffResponsibility selected() {
        List<StaffResponsibility> items = visibleItems();
        return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
    }

    private void clampSelection() {
        int size = visibleItems().size();
        selectedIndex = size == 0 ? 0 : Math.max(0, Math.min(selectedIndex, size - 1));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        StaffResponsibility item = selected();
        try {
            switch (button.id) {
                case 0:
                    board.getManager().create(titleField.getText(), descriptionField.getText(),
                            assigneeField.getText(), StaffResponsibilityPriority.NORMAL);
                    selectedIndex = visibleItems().size() - 1;
                    feedback = "Responsibility created.";
                    break;
                case 1:
                    if (item != null) {
                        StaffResponsibilityPriority[] values = StaffResponsibilityPriority.values();
                        item.setPriority(values[(item.getPriority().ordinal() + 1) % values.length]);
                        feedback = "Priority updated.";
                    }
                    break;
                case 2:
                    if (item != null) {
                        item.setAssignee(assigneeField.getText());
                        feedback = "Assignment updated.";
                    }
                    break;
                case 3:
                    if (item != null) {
                        item.setStatus(item.getStatus().next());
                        feedback = "Status updated.";
                    }
                    break;
                case 4:
                    if (item != null) {
                        board.getManager().remove(item.getId());
                        clampSelection();
                        feedback = "Responsibility deleted.";
                    }
                    break;
                case 5:
                    filterMode = (filterMode + 1) % 4;
                    button.displayString = filterMode == 0 ? "Filter: All" :
                            filterMode == 1 ? "Filter: Unassigned" :
                            filterMode == 2 ? "Filter: In Progress" : "Filter: Completed";
                    selectedIndex = 0;
                    listOffset = 0;
                    break;
                case 6: mc.displayGuiScreen(parent); return;
                case 7: selectedIndex--; clampSelection(); break;
                case 8: selectedIndex++; clampSelection(); break;
                default: break;
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            feedback = exception.getMessage();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        titleField.mouseClicked(mouseX, mouseY, mouseButton);
        descriptionField.mouseClicked(mouseX, mouseY, mouseButton);
        assigneeField.mouseClicked(mouseX, mouseY, mouseButton);
        int row = (mouseY - 42) / 18;
        if (mouseX >= width / 2 + 10 && mouseY >= 42 && row >= 0) {
            int index = listOffset + row;
            if (index < visibleItems().size()) { selectedIndex = index; feedback = ""; }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (titleField.isFocused()) titleField.textboxKeyTyped(typedChar, keyCode);
        else if (descriptionField.isFocused()) descriptionField.textboxKeyTyped(typedChar, keyCode);
        else if (assigneeField.isFocused()) assigneeField.textboxKeyTyped(typedChar, keyCode);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        titleField.updateCursorCounter();
        descriptionField.updateCursorCounter();
        assigneeField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Responsibility Board", width / 2, 12, 0xFFFFFF);
        int left = width / 2 - 150;
        drawString(fontRendererObj, "Title", left, 38, 0xAAAAAA);
        drawString(fontRendererObj, "Description", left, 66, 0xAAAAAA);
        drawString(fontRendererObj, "Assigned staff", left, 94, 0xAAAAAA);
        titleField.drawTextBox();
        descriptionField.drawTextBox();
        assigneeField.drawTextBox();

        drawString(fontRendererObj, "Responsibilities (" + visibleItems().size() + ")", width / 2 + 10, 28, 0xFFFFFF);
        List<StaffResponsibility> items = visibleItems();
        int visible = Math.max(1, (height - 95) / 18);
        if (selectedIndex < listOffset) listOffset = selectedIndex;
        if (selectedIndex >= listOffset + visible) listOffset = selectedIndex - visible + 1;
        for (int i = listOffset; i < items.size() && i < listOffset + visible; i++) {
            StaffResponsibility entry = items.get(i);
            int y = 42 + (i - listOffset) * 18;
            int color = i == selectedIndex ? 0xFFFF55 : 0xDDDDDD;
            String line = entry.getTitle() + " [" + entry.getStatus().getDisplayName() + "]";
            drawString(fontRendererObj, line.length() > 34 ? line.substring(0, 34) : line, width / 2 + 10, y, color);
        }
        StaffResponsibility current = selected();
        if (current != null) {
            drawString(fontRendererObj, "Priority: " + current.getPriority().getDisplayName(), width / 2 + 10, height - 82, 0xFFFFFF);
            drawString(fontRendererObj, "Assigned: " + (current.getAssignee().isEmpty() ? "Nobody" : current.getAssignee()), width / 2 + 10, height - 70, 0xFFFFFF);
            drawString(fontRendererObj, "Description: " + current.getDescription(), width / 2 + 10, height - 58, 0xAAAAAA);
        }
        if (!feedback.isEmpty()) drawCenteredString(fontRendererObj, feedback, width / 2, height - 12, 0xFFFF55);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
