package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

/** Create/edit form for a calendar review entry. */
public final class StaffReviewEditorGui extends GuiScreen {
    private final GuiScreen parent;
    private final StaffReviewCalendarManager manager;
    private final StaffReview existing;
    private final LocalDate defaultDate;
    private GuiTextField titleField, staffField, dateField, timeField, notesField, reminderField;
    private String feedback = "";

    public StaffReviewEditorGui(GuiScreen parent, StaffReviewCalendarManager manager,
                                LocalDate defaultDate, StaffReview existing) {
        if (manager == null || defaultDate == null) throw new IllegalArgumentException("Manager and date are required.");
        this.parent = parent;
        this.manager = manager;
        this.defaultDate = defaultDate;
        this.existing = existing;
    }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        int x = width / 2 - 130;
        titleField = field(0, x, 39, 260, 18, 60);
        staffField = field(1, x, 68, 260, 18, 32);
        dateField = field(2, x, 97, 125, 18, 10);
        timeField = field(3, x + 135, 97, 125, 18, 5);
        notesField = field(4, x, 126, 260, 18, 100);
        reminderField = field(5, x, 155, 125, 18, 3);
        if (existing == null) {
            dateField.setText(defaultDate.toString());
            reminderField.setText("1");
        } else {
            titleField.setText(existing.getTitle());
            staffField.setText(existing.getStaffName());
            dateField.setText(existing.getDate().toString());
            timeField.setText(existing.getTime() == null ? "" : existing.getTime().toString());
            notesField.setText(existing.getNotes());
            reminderField.setText(Integer.toString(existing.getReminderDaysBefore()));
        }
        buttonList.add(new GuiButton(0, width / 2 - 130, 190, 125, 20, "Save Review"));
        buttonList.add(new GuiButton(1, width / 2 + 5, 190, 125, 20, "Cancel"));
    }

    private GuiTextField field(int id, int x, int y, int w, int h, int max) {
        GuiTextField field = new GuiTextField(id, fontRendererObj, x, y, w, h);
        field.setMaxStringLength(max);
        return field;
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) { mc.displayGuiScreen(parent); return; }
        if (button.id != 0) return;
        try {
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            String timeText = timeField.getText().trim();
            LocalTime time = timeText.isEmpty() ? null : LocalTime.parse(timeText);
            int reminderDays = Integer.parseInt(reminderField.getText().trim());
            if (existing == null) {
                manager.create(date, time, titleField.getText(), staffField.getText(),
                        notesField.getText(), reminderDays);
            } else {
                manager.update(existing.getId(), date, time, titleField.getText(),
                        staffField.getText(), notesField.getText(), reminderDays);
            }
            mc.displayGuiScreen(parent);
        } catch (DateTimeParseException ex) {
            feedback = "Use YYYY-MM-DD for date and HH:mm for time.";
        } catch (NumberFormatException ex) {
            feedback = "Reminder days must be a whole number.";
        } catch (IllegalArgumentException ex) {
            feedback = ex.getMessage();
        }
    }

    @Override protected void keyTyped(char c, int key) throws IOException {
        if (titleField.textboxKeyTyped(c, key) || staffField.textboxKeyTyped(c, key)
                || dateField.textboxKeyTyped(c, key) || timeField.textboxKeyTyped(c, key)
                || notesField.textboxKeyTyped(c, key) || reminderField.textboxKeyTyped(c, key)) return;
        if (key == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(c, key);
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        titleField.mouseClicked(x, y, button); staffField.mouseClicked(x, y, button);
        dateField.mouseClicked(x, y, button); timeField.mouseClicked(x, y, button);
        notesField.mouseClicked(x, y, button); reminderField.mouseClicked(x, y, button);
    }

    @Override public void updateScreen() {
        titleField.updateCursorCounter(); staffField.updateCursorCounter();
        dateField.updateCursorCounter(); timeField.updateCursorCounter();
        notesField.updateCursorCounter(); reminderField.updateCursorCounter();
    }

    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, existing == null ? "Schedule Staff Review" : "Edit Staff Review",
                width / 2, 12, 0xFFFFFF);
        int x = width / 2 - 130;
        drawString(fontRendererObj, "Review title", x, 28, 0xAAAAAA); titleField.drawTextBox();
        drawString(fontRendererObj, "Staff member", x, 57, 0xAAAAAA); staffField.drawTextBox();
        drawString(fontRendererObj, "Date (YYYY-MM-DD)", x, 86, 0xAAAAAA);
        drawString(fontRendererObj, "Time (HH:mm, optional)", x + 135, 86, 0xAAAAAA);
        dateField.drawTextBox(); timeField.drawTextBox();
        drawString(fontRendererObj, "Notes", x, 115, 0xAAAAAA); notesField.drawTextBox();
        drawString(fontRendererObj, "Reminder days before (0-365)", x, 144, 0xAAAAAA);
        reminderField.drawTextBox();
        if (!feedback.isEmpty()) drawCenteredString(fontRendererObj, feedback, width / 2, 220, 0xFFFF5555);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
