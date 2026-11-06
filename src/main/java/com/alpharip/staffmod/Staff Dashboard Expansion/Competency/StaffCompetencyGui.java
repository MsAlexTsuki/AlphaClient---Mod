package com.alpharip.staffdashboardexpansion.competency;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

/**
 * Standalone Minecraft 1.8.9 GUI for the competency matrix.
 * The host must open this screen; no Staff Dashboard registration is included.
 */
public final class StaffCompetencyGui extends GuiScreen {
    private final GuiScreen parent;
    private final StaffCompetencyManager manager;
    private GuiTextField staffField;
    private GuiTextField competencyField;
    private GuiTextField categoryField;
    private GuiTextField notesField;
    private StaffCompetencyLevel selectedLevel = StaffCompetencyLevel.DEVELOPING;
    private int selectedIndex;
    private int scrollOffset;

    public StaffCompetencyGui(GuiScreen parent, StaffCompetencyManager manager) {
        if (manager == null) throw new IllegalArgumentException("manager must not be null.");
        this.parent = parent;
        this.manager = manager;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        int left = width / 2 - 190;
        staffField = new GuiTextField(0, fontRendererObj, left, 43, 112, 18);
        competencyField = new GuiTextField(1, fontRendererObj, left + 116, 43, 150, 18);
        categoryField = new GuiTextField(2, fontRendererObj, left + 270, 43, 110, 18);
        notesField = new GuiTextField(3, fontRendererObj, left, 83, 260, 18);
        staffField.setMaxStringLength(16);
        competencyField.setMaxStringLength(40);
        categoryField.setMaxStringLength(24);
        notesField.setMaxStringLength(80);

        buttonList.add(new GuiButton(0, left + 270, 82, 110, 20, "Add Competency"));
        buttonList.add(new GuiButton(1, left, 106, 120, 20, "Save Assessment"));
        buttonList.add(new GuiButton(2, left + 124, 106, 120, 20,
            "Level: " + selectedLevel.getDisplayName()));
        buttonList.add(new GuiButton(3, left + 248, 106, 132, 20, "Remove Assessment"));
        buttonList.add(new GuiButton(4, left, height - 28, 90, 20, "Remove Skill"));
        buttonList.add(new GuiButton(5, width / 2 + 100, height - 28, 90, 20, "Back"));
        refreshRows();
    }

    private void refreshRows() {
        while (buttonList.size() > 6) buttonList.remove(buttonList.size() - 1);
        List<StaffCompetency> competencies = manager.getCompetencies();
        int visible = Math.max(1, Math.min(9, (height - 190) / 28));
        scrollOffset = Math.max(0, Math.min(scrollOffset,
            Math.max(0, competencies.size() - visible)));
        for (int i = scrollOffset; i < competencies.size() && i < scrollOffset + visible; i++) {
            StaffCompetency competency = competencies.get(i);
            int row = i - scrollOffset;
            buttonList.add(new GuiButton(20 + row, width / 2 - 190,
                143 + row * 28, 380, 24,
                competency.getName() + "  [" + competency.getCategory() + "]"));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        if (button.id == 0) {
            String name = competencyField.getText().trim();
            String category = categoryField.getText().trim();
            if (!name.isEmpty() && !category.isEmpty()) {
                try {
                    manager.createCompetency(name, category, "");
                    competencyField.setText("");
                    refreshRows();
                } catch (IllegalArgumentException ignored) { }
            }
        } else if (button.id == 1) {
            StaffCompetency competency = getSelectedCompetency();
            if (competency != null && !staffField.getText().trim().isEmpty()) {
                try {
                    manager.assess(staffField.getText(), competency.getId(),
                        selectedLevel, notesField.getText());
                } catch (IllegalArgumentException ignored) { }
            }
        } else if (button.id == 2) {
            selectedLevel = selectedLevel.next();
            button.displayString = "Level: " + selectedLevel.getDisplayName();
        } else if (button.id == 3) {
            StaffCompetency competency = getSelectedCompetency();
            if (competency != null && !staffField.getText().trim().isEmpty()) {
                manager.removeAssessment(staffField.getText(), competency.getId());
            }
        } else if (button.id == 4) {
            StaffCompetency competency = getSelectedCompetency();
            if (competency != null) {
                manager.removeCompetency(competency.getId());
                selectedIndex = 0;
                refreshRows();
            }
        } else if (button.id == 5) {
            mc.displayGuiScreen(parent);
        } else if (button.id >= 20) {
            selectedIndex = scrollOffset + button.id - 20;
            StaffCompetency competency = getSelectedCompetency();
            if (competency != null) {
                StaffCompetencyAssessment assessment =
                    manager.getMatrix().getAssessment(staffField.getText(), competency.getId());
                if (assessment != null) {
                    selectedLevel = assessment.getLevel();
                    notesField.setText(assessment.getNotes());
                    updateLevelButton();
                }
            }
        }
    }

    private void updateLevelButton() {
        for (Object object : buttonList) {
            GuiButton button = (GuiButton) object;
            if (button.id == 2) button.displayString = "Level: " + selectedLevel.getDisplayName();
        }
    }

    private StaffCompetency getSelectedCompetency() {
        List<StaffCompetency> competencies = manager.getCompetencies();
        return selectedIndex >= 0 && selectedIndex < competencies.size()
            ? competencies.get(selectedIndex) : null;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (staffField.textboxKeyTyped(typedChar, keyCode)
                || competencyField.textboxKeyTyped(typedChar, keyCode)
                || categoryField.textboxKeyTyped(typedChar, keyCode)
                || notesField.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else if (keyCode == Keyboard.KEY_UP) {
            scrollOffset = Math.max(0, scrollOffset - 1);
            refreshRows();
        } else if (keyCode == Keyboard.KEY_DOWN) {
            scrollOffset++;
            refreshRows();
        } else super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        staffField.mouseClicked(mouseX, mouseY, mouseButton);
        competencyField.mouseClicked(mouseX, mouseY, mouseButton);
        categoryField.mouseClicked(mouseX, mouseY, mouseButton);
        notesField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset = Math.max(0, scrollOffset + (wheel > 0 ? -1 : 1));
            refreshRows();
        }
    }

    @Override
    public void updateScreen() {
        staffField.updateCursorCounter();
        competencyField.updateCursorCounter();
        categoryField.updateCursorCounter();
        notesField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Competency Matrix", width / 2, 12, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj,
            "Skills: " + manager.getMatrix().getCompetencyCount()
                + "  |  Assessments: " + manager.getMatrix().getAssessmentCount()
                + "  |  Expert: " + manager.getMatrix().count(StaffCompetencyLevel.EXPERT),
            width / 2, 27, 0xFFCCCCCC);

        int left = width / 2 - 190;
        drawString(fontRendererObj, "Staff member", left, 32, 0xFFFFFFFF);
        drawString(fontRendererObj, "Competency name", left + 116, 32, 0xFFFFFFFF);
        drawString(fontRendererObj, "Category", left + 270, 32, 0xFFFFFFFF);
        staffField.drawTextBox();
        competencyField.drawTextBox();
        categoryField.drawTextBox();
        drawString(fontRendererObj, "Assessment notes", left, 72, 0xFFFFFFFF);
        notesField.drawTextBox();

        StaffCompetency selected = getSelectedCompetency();
        if (selected != null) {
            drawString(fontRendererObj, "Selected: " + selected.getName()
                + "  |  Current level: "
                + manager.getLevel(staffField.getText(), selected.getName()).getDisplayName(),
                left, 132, 0xFFDDDDDD);
        } else {
            drawString(fontRendererObj, "Select a competency below to assess it.",
                left, 132, 0xFFAAAAAA);
        }

        if (manager.getCompetencies().isEmpty()) {
            drawCenteredString(fontRendererObj, "No competencies have been added yet.",
                width / 2, height / 2, 0xFFAAAAAA);
        }
        drawCenteredString(fontRendererObj,
            "Select a skill, enter a staff name, then save or remove its assessment.",
            width / 2, height - 43, 0xFFAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
