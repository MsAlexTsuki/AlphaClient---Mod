package com.alpharip.staffmod.staffdashboard.improvement;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/**
 * Standalone Minecraft 1.8.9 screen prototype.
 * The host module/dashboard must open this screen; no permissions or persistence are wired here.
 */
public class StaffImprovementPlanGui extends GuiScreen {
    private final GuiScreen parent;
    private final StaffImprovementManager manager;
    private GuiTextField staffField;
    private GuiTextField titleField;
    private GuiTextField objectiveField;
    private GuiTextField deadlineField;
    private GuiTextField actionField;
    private int selectedIndex = 0;
    private int listOffset = 0;
    private String feedback = "";

    public StaffImprovementPlanGui(GuiScreen parent, StaffImprovementManager manager) {
        this.parent = parent;
        this.manager = manager == null ? new StaffImprovementManager() : manager;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = width / 2 - 150;
        staffField = new GuiTextField(0, fontRendererObj, left, 48, 145, 18);
        titleField = new GuiTextField(1, fontRendererObj, left, 75, 145, 18);
        objectiveField = new GuiTextField(2, fontRendererObj, left, 102, 145, 18);
        deadlineField = new GuiTextField(3, fontRendererObj, left, 129, 145, 18);
        actionField = new GuiTextField(4, fontRendererObj, left, 220, 145, 18);
        staffField.setMaxStringLength(32);
        titleField.setMaxStringLength(48);
        objectiveField.setMaxStringLength(80);
        deadlineField.setMaxStringLength(24);
        actionField.setMaxStringLength(80);

        buttonList.add(new GuiButton(0, left, 153, 145, 20, "Create Plan"));
        buttonList.add(new GuiButton(1, left, 245, 145, 20, "Add Action"));
        buttonList.add(new GuiButton(2, left, 270, 70, 20, "Prev"));
        buttonList.add(new GuiButton(3, left + 75, 270, 70, 20, "Next"));
        buttonList.add(new GuiButton(4, left, 295, 145, 20, "Cycle Status"));
        buttonList.add(new GuiButton(5, left, 320, 145, 20, "Delete Plan"));
        buttonList.add(new GuiButton(6, width / 2 + 10, 320, 140, 20, "Back"));
        clampSelection();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        StaffImprovementPlan selected = getSelectedPlan();
        try {
            switch (button.id) {
                case 0:
                    manager.createPlan(staffField.getText(), titleField.getText(), objectiveField.getText(), deadlineField.getText());
                    selectedIndex = manager.getPlans().size() - 1;
                    feedback = "Plan created.";
                    break;
                case 1:
                    if (selected == null) { feedback = "Select a plan first."; break; }
                    manager.addAction(selected.getId(), actionField.getText());
                    actionField.setText("");
                    feedback = "Action added.";
                    break;
                case 2: selectedIndex--; clampSelection(); break;
                case 3: selectedIndex++; clampSelection(); break;
                case 4:
                    if (selected != null) { selected.setStatus(selected.getStatus().next()); feedback = "Status updated."; }
                    break;
                case 5:
                    if (selected != null) { manager.removePlan(selected.getId()); clampSelection(); feedback = "Plan deleted."; }
                    break;
                case 6: mc.displayGuiScreen(parent); return;
                default: break;
            }
        } catch (IllegalArgumentException exception) {
            feedback = exception.getMessage();
        }
    }

    private StaffImprovementPlan getSelectedPlan() {
        List<StaffImprovementPlan> plans = manager.getPlans();
        return selectedIndex >= 0 && selectedIndex < plans.size() ? plans.get(selectedIndex) : null;
    }

    private void clampSelection() {
        int size = manager.getPlans().size();
        if (size == 0) selectedIndex = 0;
        else selectedIndex = Math.max(0, Math.min(selectedIndex, size - 1));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        staffField.mouseClicked(mouseX, mouseY, mouseButton);
        titleField.mouseClicked(mouseX, mouseY, mouseButton);
        objectiveField.mouseClicked(mouseX, mouseY, mouseButton);
        deadlineField.mouseClicked(mouseX, mouseY, mouseButton);
        actionField.mouseClicked(mouseX, mouseY, mouseButton);
        int listTop = 48;
        int row = (mouseY - listTop) / 20;
        if (mouseX >= width / 2 + 10 && mouseY >= listTop && row >= 0) {
            int index = listOffset + row;
            if (index < manager.getPlans().size()) { selectedIndex = index; feedback = ""; }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (staffField.isFocused()) staffField.textboxKeyTyped(typedChar, keyCode);
        else if (titleField.isFocused()) titleField.textboxKeyTyped(typedChar, keyCode);
        else if (objectiveField.isFocused()) objectiveField.textboxKeyTyped(typedChar, keyCode);
        else if (deadlineField.isFocused()) deadlineField.textboxKeyTyped(typedChar, keyCode);
        else if (actionField.isFocused()) actionField.textboxKeyTyped(typedChar, keyCode);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        staffField.updateCursorCounter();
        titleField.updateCursorCounter();
        objectiveField.updateCursorCounter();
        deadlineField.updateCursorCounter();
        actionField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Improvement Plan", width / 2, 12, 0xFFFFFF);
        int left = width / 2 - 150;
        drawString(fontRendererObj, "Staff member", left, 38, 0xAAAAAA);
        drawString(fontRendererObj, "Plan title", left, 65, 0xAAAAAA);
        drawString(fontRendererObj, "Objective", left, 92, 0xAAAAAA);
        drawString(fontRendererObj, "Deadline (optional)", left, 119, 0xAAAAAA);
        staffField.drawTextBox();
        titleField.drawTextBox();
        objectiveField.drawTextBox();
        deadlineField.drawTextBox();
        drawString(fontRendererObj, "Action item", left, 210, 0xAAAAAA);
        actionField.drawTextBox();

        drawString(fontRendererObj, "Plans (" + manager.getTotalPlanCount() + ") - select on right", width / 2 + 10, 34, 0xFFFFFF);
        List<StaffImprovementPlan> plans = manager.getPlans();
        int visible = Math.max(1, (height - 90) / 20);
        if (selectedIndex < listOffset) listOffset = selectedIndex;
        if (selectedIndex >= listOffset + visible) listOffset = selectedIndex - visible + 1;
        for (int i = listOffset; i < plans.size() && i < listOffset + visible; i++) {
            StaffImprovementPlan plan = plans.get(i);
            int y = 48 + (i - listOffset) * 20;
            int color = i == selectedIndex ? 0xFFFF55 : 0xDDDDDD;
            String line = plan.getStaffMember() + " - " + plan.getTitle();
            drawString(fontRendererObj, line.length() > 34 ? line.substring(0, 34) : line, width / 2 + 10, y, color);
        }

        StaffImprovementPlan selected = getSelectedPlan();
        if (selected != null) {
            drawString(fontRendererObj, "Status: " + selected.getStatus().getDisplayName(), width / 2 + 10, height - 72, 0xFFFFFF);
            drawString(fontRendererObj, "Progress: " + selected.getProgressPercent() + "%", width / 2 + 10, height - 60, 0xFFFFFF);
            drawString(fontRendererObj, "Actions: " + selected.getCompletedActionCount() + "/" + selected.getActionCount(), width / 2 + 10, height - 48, 0xFFFFFF);
            drawString(fontRendererObj, "Deadline: " + (selected.getDeadline().isEmpty() ? "Not set" : selected.getDeadline()), width / 2 + 10, height - 36, 0xAAAAAA);
        }
        if (!feedback.isEmpty()) drawCenteredString(fontRendererObj, feedback, width / 2, height - 12, 0xFFFF55);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
