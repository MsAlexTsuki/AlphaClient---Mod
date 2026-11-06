package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Detailed agenda for one selected calendar date. */
public final class StaffReviewDayGui extends GuiScreen {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private final GuiScreen parent;
    private final StaffReviewCalendar calendar;
    private final LocalDate date;
    private int selectedIndex = -1;
    private int scrollOffset;

    public StaffReviewDayGui(GuiScreen parent, StaffReviewCalendar calendar, LocalDate date) {
        if (calendar == null || date == null) throw new IllegalArgumentException("Calendar and date are required.");
        this.parent = parent;
        this.calendar = calendar;
        this.date = date;
    }

    private List<StaffReview> reviews() { return calendar.getManager().getForDate(date); }
    private StaffReview selected() {
        List<StaffReview> items = reviews();
        return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
    }

    @Override public void initGui() {
        buttonList.clear();
        int y = height - 28;
        buttonList.add(new GuiButton(0, width / 2 - 202, y, 78, 20, "Add"));
        buttonList.add(new GuiButton(1, width / 2 - 120, y, 78, 20, "Edit"));
        buttonList.add(new GuiButton(2, width / 2 - 120, y, 78, 20, "Status"));
        buttonList.add(new GuiButton(3, width / 2 - 38, y, 78, 20, "Ack Reminder"));
        buttonList.add(new GuiButton(4, width / 2 + 44, y, 78, 20, "Delete"));
        buttonList.add(new GuiButton(5, width / 2 + 126, y, 78, 20, "Back"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        StaffReview item = selected();
        if (button.id == 0) mc.displayGuiScreen(new StaffReviewEditorGui(this, calendar.getManager(), date, null));
        else if (button.id == 1 && item != null) mc.displayGuiScreen(new StaffReviewEditorGui(this, calendar.getManager(), date, item));
        else if (button.id == 2 && item != null) {
            item.setStatus(item.getStatus().next());
        } else if (button.id == 3 && item != null) {
            calendar.getManager().remove(item.getId());
            selectedIndex = Math.min(selectedIndex, reviews().size() - 1);
        } else if (button.id == 4) mc.displayGuiScreen(parent);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;
        int rowHeight = 34;
        int top = 45;
        for (int i = scrollOffset; i < reviews().size(); i++) {
            int y = top + (i - scrollOffset) * rowHeight;
            if (mouseY >= y && mouseY < y + rowHeight) { selectedIndex = i; return; }
        }
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int visible = Math.max(1, (height - 90) / 34);
            scrollOffset = Math.max(0, Math.min(Math.max(0, reviews().size() - visible),
                    scrollOffset + (wheel > 0 ? -1 : 1)));
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Review Agenda — " + date, width / 2, 12, 0xFFFFFF);
        List<StaffReview> items = reviews();
        int visible = Math.max(1, (height - 90) / 34);
        if (selectedIndex >= items.size()) selectedIndex = items.size() - 1;
        if (selectedIndex < scrollOffset) scrollOffset = Math.max(0, selectedIndex);
        if (selectedIndex >= scrollOffset + visible) scrollOffset = selectedIndex - visible + 1;
        for (int i = scrollOffset; i < items.size() && i < scrollOffset + visible; i++) {
            StaffReview item = items.get(i);
            int y = 45 + (i - scrollOffset) * 34;
            drawRect(width / 2 - 190, y, width / 2 + 190, y + 30,
                    i == selectedIndex ? 0xAA665522 : 0x88303030);
            String time = item.getTime() == null ? "No time" : item.getTime().format(TIME);
            drawString(fontRendererObj, trim(time + " | " + item.getTitle(), 62),
                    width / 2 - 182, y + 4, 0xFFFFFF);
            String status = item.isOverdue(LocalDate.now()) ? "OVERDUE" : item.getStatus().getDisplayName();
            drawString(fontRendererObj, trim(item.getStaffName() + " | " + status, 62),
                    width / 2 - 182, y + 17, item.isOverdue(LocalDate.now()) ? 0xFFFF5555 : 0xFFBBBBBB);
        }
        if (items.isEmpty()) drawCenteredString(fontRendererObj, "No reviews scheduled for this date.",
                width / 2, height / 2, 0xAAAAAA);
        StaffReview current = selected();
        if (current != null) drawString(fontRendererObj, "Reminder: " + current.getReminderDaysBefore()
                + " day(s) before | " + (current.isReminderAcknowledged() ? "Acknowledged" : "Pending")
                + " | " + current.getNotes(), 8, height - 43, 0xFFCCCCCC);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String trim(String value, int max) { return value.length() <= max ? value : value.substring(0, max - 3) + "..."; }
    @Override public boolean doesGuiPauseGame() { return false; }
}
