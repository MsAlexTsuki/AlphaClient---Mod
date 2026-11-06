package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Twelve-month year overview with real date alignment and review markers. */
public final class StaffReviewCalendarGui extends GuiScreen {
    private static final int GRID_TOP = 48;
    private static final int MONTH_ROW_HEIGHT = 122;
    private static final int DAY_ROW_HEIGHT = 12;
    private final GuiScreen parent;
    private final StaffReviewCalendar calendar;
    private LocalDate selectedDate;

    public StaffReviewCalendarGui(GuiScreen parent, StaffReviewCalendar calendar) {
        if (calendar == null) throw new IllegalArgumentException("Calendar is required.");
        this.parent = parent;
        this.calendar = calendar;
        this.selectedDate = LocalDate.now().withYear(calendar.getDisplayedYear());
    }

    @Override public void initGui() {
        buttonList.clear();
        int y = height - 27;
        int w = Math.max(62, (width - 28) / 6);
        int start = (width - (w * 6 + 5 * 4)) / 2;
        buttonList.add(new GuiButton(0, start, y, w, 20, "< Year"));
        buttonList.add(new GuiButton(1, start + (w + 4), y, w, 20, "Year >"));
        buttonList.add(new GuiButton(2, start + (w + 4) * 2, y, w, 20, "Today"));
        buttonList.add(new GuiButton(3, start + (w + 4) * 3, y, w, 20, "Day details"));
        buttonList.add(new GuiButton(4, start + (w + 4) * 4, y, w, 20, "Add review"));
        buttonList.add(new GuiButton(5, start + (w + 4) * 5, y, w, 20, "Back"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            calendar.showPreviousYear();
            selectedDate = safeDate(calendar.getDisplayedYear(), selectedDate.getMonthValue(), selectedDate.getDayOfMonth());
        } else if (button.id == 1) {
            calendar.showNextYear();
            selectedDate = safeDate(calendar.getDisplayedYear(), selectedDate.getMonthValue(), selectedDate.getDayOfMonth());
        } else if (button.id == 2) {
            calendar.showCurrentYear();
            selectedDate = LocalDate.now();
        } else if (button.id == 3) {
            mc.displayGuiScreen(new StaffReviewDayGui(this, calendar, selectedDate));
        } else if (button.id == 4) {
            mc.displayGuiScreen(new StaffReviewEditorGui(this, calendar.getManager(), selectedDate, null));
        } else if (button.id == 5) {
            mc.displayGuiScreen(parent);
        }
    }

    private LocalDate safeDate(int year, int month, int day) {
        int safeDay = Math.min(day, Month.of(month).length(LocalDate.of(year, 1, 1).isLeapYear()));
        return LocalDate.of(year, month, safeDay);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;
        int monthWidth = (width - 24) / 4;
        int cellWidth = monthWidth / 7;
        for (int month = 1; month <= 12; month++) {
            int column = (month - 1) % 4;
            int row = (month - 1) / 4;
            int left = 8 + column * (monthWidth + 2);
            int top = GRID_TOP + row * MONTH_ROW_HEIGHT;
            int dayTop = top + 27;
            if (mouseX < left || mouseX >= left + monthWidth || mouseY < dayTop || mouseY >= dayTop + 6 * DAY_ROW_HEIGHT) continue;
            int dayColumn = (mouseX - left) / Math.max(1, cellWidth);
            int week = (mouseY - dayTop) / DAY_ROW_HEIGHT;
            LocalDate first = LocalDate.of(calendar.getDisplayedYear(), month, 1);
            int offset = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            int day = week * 7 + dayColumn - offset + 1;
            if (day >= 1 && day <= first.lengthOfMonth()) selectedDate = first.withDayOfMonth(day);
            return;
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Review Calendar — " + calendar.getDisplayedYear(),
                width / 2, 8, 0xFFFFFF);
        drawCenteredString(fontRendererObj, calendar.getManager().countForYear(calendar.getDisplayedYear())
                + " reviews scheduled this year", width / 2, 21, 0xAAAAAA);
        int monthWidth = (width - 24) / 4;
        int cellWidth = monthWidth / 7;
        String[] weekdays = {"M", "T", "W", "T", "F", "S", "S"};
        for (int month = 1; month <= 12; month++) {
            int column = (month - 1) % 4;
            int row = (month - 1) / 4;
            int left = 8 + column * (monthWidth + 2);
            int top = GRID_TOP + row * MONTH_ROW_HEIGHT;
            Month monthValue = Month.of(month);
            drawString(fontRendererObj, monthValue.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    left, top, 0xFFFFFF);
            LocalDate first = LocalDate.of(calendar.getDisplayedYear(), month, 1);
            int offset = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            for (int col = 0; col < 7; col++) {
                drawString(fontRendererObj, weekdays[col], left + col * cellWidth + 2, top + 14, 0x888888);
            }
            for (int day = 1; day <= first.lengthOfMonth(); day++) {
                int index = offset + day - 1;
                int x = left + (index % 7) * cellWidth;
                int y = top + 27 + (index / 7) * DAY_ROW_HEIGHT;
                LocalDate date = first.withDayOfMonth(day);
                int count = calendar.getManager().countForDate(date);
                boolean selected = date.equals(selectedDate);
                if (selected) drawRect(x, y - 1, x + cellWidth - 1, y + 11, 0xAA806020);
                int color = date.equals(LocalDate.now()) ? 0xFF55FFFF : 0xFFDDDDDD;
                String dayText = Integer.toString(day);
                drawString(fontRendererObj, dayText, x + 2, y, color);
                if (count > 0) {
                    drawRect(x + cellWidth - 5, y + 7, x + cellWidth - 2, y + 10, 0xFFFFC83D);
                    if (count > 1) drawString(fontRendererObj, Integer.toString(Math.min(count, 9)), x + cellWidth - 9, y, 0xFFFFC83D);
                }
            }
        }
        drawString(fontRendererObj, "Selected: " + selectedDate + "  |  Gold marker = review scheduled",
                8, height - 42, 0xDDDDDD);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
