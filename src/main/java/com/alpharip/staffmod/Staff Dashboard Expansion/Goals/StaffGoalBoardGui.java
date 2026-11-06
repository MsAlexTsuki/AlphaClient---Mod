package com.alpharip.staffdashboardexpansion.goals;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Minecraft 1.8.9 GUI for the standalone Staff Goal Board prototype.
 *
 * The host must open this screen. Dashboard registration, staff identity,
 * permissions, and shared persistence are intentionally not wired here.
 */
public final class StaffGoalBoardGui extends GuiScreen {
    private final StaffGoalBoard board;
    private GuiTextField titleField;
    private GuiTextField teamField;
    private GuiTextField daysField;
    private List<StaffGoal> visibleGoals;
    private int selectedIndex;
    private int scrollOffset;
    private StaffGoalStatus statusFilter;
    private String teamFilter;

    public StaffGoalBoardGui(StaffGoalBoard board) {
        if (board == null) {
            throw new IllegalArgumentException("board must not be null.");
        }
        this.board = board;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;

        titleField = new GuiTextField(0, fontRendererObj, center - 190, 43, 150, 18);
        teamField = new GuiTextField(1, fontRendererObj, center - 34, 43, 90, 18);
        daysField = new GuiTextField(2, fontRendererObj, center + 62, 43, 42, 18);
        titleField.setMaxStringLength(48);
        teamField.setMaxStringLength(24);
        daysField.setMaxStringLength(3);
        daysField.setText("7");

        buttonList.add(new GuiButton(0, center + 110, 42, 80, 20, "Add Goal"));
        buttonList.add(new GuiButton(1, center - 190, height - 29, 90, 20, "Remove"));
        buttonList.add(new GuiButton(2, center - 96, height - 29, 100, 20, "Next Status"));
        buttonList.add(new GuiButton(3, center + 8, height - 29, 90, 20, "Status: All"));
        buttonList.add(new GuiButton(4, center + 102, height - 29, 88, 20, "Team: All"));

        refreshGoals();
        updateFilterButtonLabels();
    }

    private void refreshGoals() {
        visibleGoals = board.snapshot(teamFilter, statusFilter);
        if (selectedIndex >= visibleGoals.size()) {
            selectedIndex = Math.max(0, visibleGoals.size() - 1);
        }
        int maxOffset = Math.max(0, visibleGoals.size() - getVisibleRowCount());
        scrollOffset = Math.min(scrollOffset, maxOffset);
    }

    private int getVisibleRowCount() {
        return Math.max(1, (height - 126) / 34);
    }

    private void updateFilterButtonLabels() {
        for (Object object : buttonList) {
            GuiButton button = (GuiButton) object;
            if (button.id == 3) {
                button.displayString = statusFilter == null
                    ? "Status: All" : "Status: " + statusFilter.getDisplayName();
            } else if (button.id == 4) {
                button.displayString = teamFilter == null
                    ? "Team: All" : "Team: " + trim(teamFilter, 10);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            addGoalFromFields();
        } else if (button.id == 1) {
            StaffGoal selected = getSelectedGoal();
            if (selected != null) {
                board.removeGoal(selected.getId());
            }
        } else if (button.id == 2) {
            StaffGoal selected = getSelectedGoal();
            if (selected != null) {
                board.setStatus(selected.getId(), selected.getStatus().next());
            }
        } else if (button.id == 3) {
            cycleStatusFilter();
        } else if (button.id == 4) {
            cycleTeamFilter();
        }
        refreshGoals();
        updateFilterButtonLabels();
    }

    private void addGoalFromFields() {
        String title = titleField.getText().trim();
        String team = teamField.getText().trim();
        if (title.isEmpty() || team.isEmpty()) {
            return;
        }

        int days;
        try {
            days = Integer.parseInt(daysField.getText().trim());
        } catch (NumberFormatException exception) {
            return;
        }
        if (days < 0 || days > 365) {
            return;
        }

        long dueAt = days == 0 ? 0L
            : System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L;
        board.createGoal(team, title, "", dueAt);
        titleField.setText("");
        refreshGoals();
    }

    private void cycleStatusFilter() {
        if (statusFilter == null) {
            statusFilter = StaffGoalStatus.OPEN;
        } else if (statusFilter == StaffGoalStatus.CANCELLED) {
            statusFilter = null;
        } else {
            statusFilter = statusFilter.next();
        }
        selectedIndex = 0;
        scrollOffset = 0;
    }

    private void cycleTeamFilter() {
        List<String> teams = board.getTeams();
        if (teams.isEmpty()) {
            teamFilter = null;
            return;
        }
        if (teamFilter == null) {
            teamFilter = teams.get(0);
        } else {
            int index = -1;
            for (int i = 0; i < teams.size(); i++) {
                if (teams.get(i).equalsIgnoreCase(teamFilter)) {
                    index = i;
                    break;
                }
            }
            teamFilter = index + 1 >= teams.size() ? null : teams.get(index + 1);
        }
        selectedIndex = 0;
        scrollOffset = 0;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        titleField.mouseClicked(mouseX, mouseY, mouseButton);
        teamField.mouseClicked(mouseX, mouseY, mouseButton);
        daysField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) {
            return;
        }
        int firstRowY = 91;
        int rowHeight = 34;
        for (int i = 0; i < getVisibleRowCount(); i++) {
            int index = scrollOffset + i;
            if (index >= visibleGoals.size()) {
                break;
            }
            int rowY = firstRowY + i * rowHeight;
            if (mouseY >= rowY && mouseY < rowY + 30
                    && mouseX >= width / 2 - 190 && mouseX <= width / 2 + 190) {
                selectedIndex = index;
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (titleField.textboxKeyTyped(typedChar, keyCode)
                || teamField.textboxKeyTyped(typedChar, keyCode)
                || daysField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        if (keyCode == 200) {
            scrollOffset = Math.max(0, scrollOffset - 1);
            return;
        }
        if (keyCode == 208) {
            int maxOffset = Math.max(0, visibleGoals.size() - getVisibleRowCount());
            scrollOffset = Math.min(maxOffset, scrollOffset + 1);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        titleField.updateCursorCounter();
        teamField.updateCursorCounter();
        daysField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Goal Board", width / 2, 12, 0xFFFFFFFF);
        String summary = "Goals: " + board.size()
            + "  |  Open: " + board.count(StaffGoalStatus.OPEN)
            + "  |  In Progress: " + board.count(StaffGoalStatus.IN_PROGRESS)
            + "  |  Overdue: " + board.countOverdue(System.currentTimeMillis());
        drawCenteredString(fontRendererObj, summary, width / 2, 27, 0xFFCCCCCC);

        drawString(fontRendererObj, "Title", width / 2 - 190, 32, 0xFFFFFFFF);
        drawString(fontRendererObj, "Team", width / 2 - 34, 32, 0xFFFFFFFF);
        drawString(fontRendererObj, "Days (0 = none)", width / 2 + 62, 32, 0xFFFFFFFF);
        titleField.drawTextBox();
        teamField.drawTextBox();
        daysField.drawTextBox();

        int firstRowY = 91;
        int rowHeight = 34;
        for (int i = 0; i < getVisibleRowCount(); i++) {
            int index = scrollOffset + i;
            if (index >= visibleGoals.size()) {
                break;
            }
            StaffGoal goal = visibleGoals.get(index);
            int rowY = firstRowY + i * rowHeight;
            boolean selected = index == selectedIndex;
            int background = selected ? 0xAA665522 : 0x88303030;
            drawRect(width / 2 - 190, rowY, width / 2 + 190, rowY + 30, background);

            String deadline = goal.hasDeadline()
                ? new SimpleDateFormat("yyyy-MM-dd").format(new Date(goal.getDueAt()))
                : "No deadline";
            String firstLine = goal.getTitle() + "  [" + goal.getTeam() + "]";
            drawString(fontRendererObj, trim(firstLine, 62),
                width / 2 - 182, rowY + 4, 0xFFFFFFFF);

            String secondLine = goal.getStatus().getDisplayName()
                + "  |  " + goal.getProgress() + "%  |  Due: " + deadline;
            int statusColor = goal.isOverdue(System.currentTimeMillis())
                ? 0xFFFF7777 : 0xFFBBBBBB;
            drawString(fontRendererObj, trim(secondLine, 70),
                width / 2 - 182, rowY + 17, statusColor);
        }

        if (visibleGoals.isEmpty()) {
            drawCenteredString(fontRendererObj, "No goals match the selected filters.",
                width / 2, height / 2, 0xFFAAAAAA);
        }
        drawCenteredString(fontRendererObj,
            "Select a row, then use Remove or Next Status. Up/Down scrolls the list.",
            width / 2, height - 43, 0xFFAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private StaffGoal getSelectedGoal() {
        return selectedIndex >= 0 && selectedIndex < visibleGoals.size()
            ? visibleGoals.get(selectedIndex) : null;
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
