package com.alpharip.staffdashboardexpansion.coverage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/** Standalone Minecraft 1.8.9 GUI; the host must open it and provide the shared shift manager. */
public final class StaffCoverageForecastGui extends GuiScreen {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final CoverageForecastManager manager;
    private GuiTextField dateField, nameField, startField, endField, minimumField;
    private LocalDate selectedDate = LocalDate.now();
    private int scrollOffset;
    private int selectedIndex = -1;
    private String message = "Uses locally scheduled shifts only.";

    public StaffCoverageForecastGui(CoverageForecastManager manager) {
        if (manager == null) throw new IllegalArgumentException("manager cannot be null.");
        this.manager = manager;
    }

    @Override public void initGui() {
        buttonList.clear();
        int x = width / 2 - 150;
        dateField = new GuiTextField(10, fontRendererObj, x, 38, 100, 18);
        dateField.setMaxStringLength(10); dateField.setText(selectedDate.format(DATE));
        nameField = new GuiTextField(11, fontRendererObj, x, 83, 105, 18);
        nameField.setMaxStringLength(24);
        startField = new GuiTextField(12, fontRendererObj, x + 110, 83, 55, 18);
        startField.setMaxStringLength(5); startField.setText("18:00");
        endField = new GuiTextField(13, fontRendererObj, x + 170, 83, 55, 18);
        endField.setMaxStringLength(5); endField.setText("20:00");
        minimumField = new GuiTextField(14, fontRendererObj, x + 230, 83, 35, 18);
        minimumField.setMaxStringLength(2); minimumField.setText("2");
        buttonList.add(new GuiButton(0, x + 110, 37, 75, 20, "Load date"));
        buttonList.add(new GuiButton(1, x + 190, 37, 75, 20, "Today"));
        buttonList.add(new GuiButton(2, x + 270, 82, 70, 20, "Add target"));
        buttonList.add(new GuiButton(3, x + 270, 107, 70, 20, "Remove last"));
        buttonList.add(new GuiButton(4, x + 110, height - 30, 90, 20, "Refresh"));
        buttonList.add(new GuiButton(5, x + 210, height - 30, 90, 20, "Done"));
    }

    @Override protected void actionPerformed(GuiButton button) {
        try {
            if (button.id == 0) {
                selectedDate = LocalDate.parse(dateField.getText().trim(), DATE);
                scrollOffset = 0; selectedIndex = -1;
            } else if (button.id == 1) {
                selectedDate = LocalDate.now(); dateField.setText(selectedDate.format(DATE)); scrollOffset = 0; selectedIndex = -1;
            } else if (button.id == 2) {
                LocalDateTime from = LocalDateTime.of(selectedDate, LocalTime.parse(startField.getText().trim(), TIME));
                LocalDateTime to = LocalDateTime.of(selectedDate, LocalTime.parse(endField.getText().trim(), TIME));
                if (!to.isAfter(from)) throw new IllegalArgumentException("End must be after start on the same date.");
                int minimum = Integer.parseInt(minimumField.getText().trim());
                manager.addRequirement(nameField.getText().trim(), from, to, minimum);
                message = "Coverage target added.";
            } else if (button.id == 3) {
                List<CoverageResult> visible = manager.getResults(selectedDate);
                if (selectedIndex < 0 || selectedIndex >= visible.size()) message = "Select a target row first.";
                else {
                    manager.removeRequirement(visible.get(selectedIndex).getRequirement().getId());
                    selectedIndex = -1;
                    message = "Selected target removed.";
                }
            } else if (button.id == 4) {
                scrollOffset = 0; message = "Forecast view refreshed.";
            } else if (button.id == 5) mc.displayGuiScreen(null);
        } catch (DateTimeParseException ex) {
            message = "Use yyyy-MM-dd for date and HH:mm for time.";
        } catch (NumberFormatException ex) {
            message = "Minimum staff must be a whole number.";
        } catch (IllegalArgumentException ex) {
            message = ex.getMessage();
        }
    }

    @Override public void updateScreen() {
        dateField.updateCursorCounter(); nameField.updateCursorCounter();
        startField.updateCursorCounter(); endField.updateCursorCounter(); minimumField.updateCursorCounter();
    }

    @Override protected void keyTyped(char c, int key) throws IOException {
        if (key == 1) { mc.displayGuiScreen(null); return; }
        dateField.textboxKeyTyped(c, key); nameField.textboxKeyTyped(c, key);
        startField.textboxKeyTyped(c, key); endField.textboxKeyTyped(c, key);
        minimumField.textboxKeyTyped(c, key); super.keyTyped(c, key);
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        if (y >= 151 && y < height - 48) {
            int row = (y - 151) / 24 + scrollOffset;
            List<CoverageResult> visible = manager.getResults(selectedDate);
            if (row >= 0 && row < visible.size()) selectedIndex = row;
        }
        dateField.mouseClicked(x, y, button); nameField.mouseClicked(x, y, button);
        startField.mouseClicked(x, y, button); endField.mouseClicked(x, y, button);
        minimumField.mouseClicked(x, y, button);
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) scrollOffset = Math.max(0, scrollOffset + (wheel > 0 ? -2 : 2));
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Coverage Forecast", width / 2, 12, 0xFFFFFF);
        drawString(fontRendererObj, "Date (yyyy-MM-dd)", width / 2 - 150, 27, 0xCCCCCC);
        dateField.drawTextBox();
        drawString(fontRendererObj, "Requirement", width / 2 - 150, 69, 0xCCCCCC);
        drawString(fontRendererObj, "Start", width / 2 - 40, 69, 0xCCCCCC);
        drawString(fontRendererObj, "End", width / 2 + 20, 69, 0xCCCCCC);
        drawString(fontRendererObj, "Min", width / 2 + 80, 69, 0xCCCCCC);
        nameField.drawTextBox(); startField.drawTextBox(); endField.drawTextBox(); minimumField.drawTextBox();
        List<CoverageResult> results = manager.getResults(selectedDate);
        drawCenteredString(fontRendererObj, "Covered: " + manager.countCovered(selectedDate)
                + "   Understaffed: " + manager.countUnderstaffed(selectedDate)
                + "   Targets: " + results.size(), width / 2, 132, 0xAAAAAA);
        int y = 151, rowHeight = 24, rows = Math.max(1, (height - 190) / rowHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, results.size() - rows)));
        for (int i = scrollOffset; i < results.size() && i < scrollOffset + rows; i++) {
            CoverageResult r = results.get(i); CoverageRequirement q = r.getRequirement();
            int rowY = y + (i - scrollOffset) * rowHeight;
            int color = r.isUnderstaffed() ? 0xFF6666 : 0x66FF66;
            if (i == selectedIndex) drawRect(width / 2 - 153, rowY - 2, width / 2 + 153, rowY + 18, 0x55333333);
            String line = q.getName() + " " + q.getStart().toLocalTime().format(TIME)
                    + "-" + q.getEnd().toLocalTime().format(TIME) + " "
                    + r.getScheduledStaff() + "/" + r.getMinimumStaff();
            drawString(fontRendererObj, line.length() > 48 ? line.substring(0, 45) + "..." : line,
                    width / 2 - 150, rowY, color);
            drawString(fontRendererObj, r.isUnderstaffed() ? "SHORT " + r.getShortage() : "OK",
                    width / 2 + 105, rowY, color);
        }
        if (results.isEmpty()) drawCenteredString(fontRendererObj, "No coverage targets for this date.", width / 2, 170, 0xAAAAAA);
        drawCenteredString(fontRendererObj, message, width / 2, height - 43, 0x888888);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // TODO: Register this screen in the Staff Dashboard only after explicit integration approval.
}