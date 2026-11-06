package com.alpharip.staffmod.staffdashboardexpansion.reviewcalendar;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * Status board for the reviews managed by Staff Review Calendar.
 * It deliberately shares the calendar manager instead of maintaining duplicate records.
 */
public final class StaffReviewBoardGui extends GuiScreen {
    private static final int LIST_TOP = 52;
    private static final int ROW_HEIGHT = 38;

    private final GuiScreen parent;
    private final StaffReviewCalendar calendar;
    private Integer selectedReviewId;
    private int scrollOffset;

    public StaffReviewBoardGui(GuiScreen parent, StaffReviewCalendar calendar) {
        if (calendar == null) throw new IllegalArgumentException("Calendar is required.");
        this.parent = parent;
        this.calendar = calendar;
    }

    @Override public void initGui() {
        buttonList.clear();
        int buttonWidth = 82;
        int gap = 4;
        int start = width / 2 - (buttonWidth * 5 + gap * 4) / 2;
        int y = height - 27;
        buttonList.add(new GuiButton(0, start, y, buttonWidth, 20, "Add review"));
        buttonList.add(new GuiButton(1, start + buttonWidth + gap, y, buttonWidth, 20, "Edit"));
        buttonList.add(new GuiButton(2, start + (buttonWidth + gap) * 2, y, buttonWidth, 20, "Next status"));
        buttonList.add(new GuiButton(3, start + (buttonWidth + gap) * 3, y, buttonWidth, 20, "Delete"));
        buttonList.add(new GuiButton(4, start + (buttonWidth + gap) * 4, y, buttonWidth, 20, "Back"));
    }

    private List<StaffReview> getColumn(StaffReviewStatus status) {
        List<StaffReview> result = new ArrayList<StaffReview>();
        for (StaffReview review : calendar.getManager().getAll()) {
            if (review.getStatus() == status) result.add(review);
        }
        return result;
    }

    private StaffReview getSelectedReview() {
        return selectedReviewId == null ? null : calendar.getManager().get(selectedReviewId.intValue());
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        StaffReview selected = getSelectedReview();
        if (button.id == 0) {
            mc.displayGuiScreen(new StaffReviewEditorGui(this, calendar.getManager(), LocalDate.now(), null));
        } else if (button.id == 1 && selected != null) {
            mc.displayGuiScreen(new StaffReviewEditorGui(this, calendar.getManager(), selected.getDate(), selected));
        } else if (button.id == 2 && selected != null) {
            calendar.getManager().setStatus(selected.getId(), selected.getStatus().next());
        } else if (button.id == 3 && selected != null) {
            calendar.getManager().remove(selected.getId());
            selectedReviewId = null;
        } else if (button.id == 4) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 || mouseY < LIST_TOP) return;

        int columnWidth = (width - 32) / 3;
        int columnStep = columnWidth + 8;
        int column = (mouseX - 16) / Math.max(1, columnStep);
        if (column < 0 || column > 2 || mouseX >= 16 + column * columnStep + columnWidth) return;

        int row = (mouseY - LIST_TOP) / ROW_HEIGHT;
        int visibleRows = Math.max(1, (height - LIST_TOP - 48) / ROW_HEIGHT);
        if (row < 0 || row >= visibleRows) return;

        StaffReviewStatus status = StaffReviewStatus.values()[column];
        List<StaffReview> items = getColumn(status);
        int index = scrollOffset + row;
        if (index < items.size()) selectedReviewId = Integer.valueOf(items.get(index).getId());
    }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            int visibleRows = Math.max(1, (height - LIST_TOP - 48) / ROW_HEIGHT);
            int largestColumn = Math.max(getColumn(StaffReviewStatus.SCHEDULED).size(),
                    Math.max(getColumn(StaffReviewStatus.COMPLETED).size(),
                            getColumn(StaffReviewStatus.CANCELLED).size()));
            int maxOffset = Math.max(0, largestColumn - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + (wheel > 0 ? -1 : 1)));
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Staff Review Board", width / 2, 10, 0xFFFFFF);
        drawCenteredString(fontRendererObj, calendar.getManager().size() + " total reviews",
                width / 2, 23, 0xAAAAAA);

        int columnWidth = (width - 32) / 3;
        int columnStep = columnWidth + 8;
        int visibleRows = Math.max(1, (height - LIST_TOP - 48) / ROW_HEIGHT);
        int[] accents = {0xFFFFC83D, 0xFF55CC88, 0xFF999999};

        for (StaffReviewStatus status : StaffReviewStatus.values()) {
            int column = status.ordinal();
            int left = 16 + column * columnStep;
            List<StaffReview> items = getColumn(status);
            drawRect(left, 39, left + columnWidth, 41, accents[column]);
            drawString(fontRendererObj, status.getDisplayName() + " (" + items.size() + ")",
                    left + 3, 28, 0xFFFFFF);

            for (int row = 0; row < visibleRows; row++) {
                int index = scrollOffset + row;
                if (index >= items.size()) break;
                StaffReview review = items.get(index);
                int top = LIST_TOP + row * ROW_HEIGHT;
                boolean selected = selectedReviewId != null && selectedReviewId.intValue() == review.getId();
                boolean overdue = review.isOverdue(LocalDate.now());
                int background = selected ? 0xAA665522 : 0x88303030;
                drawRect(left, top, left + columnWidth, top + ROW_HEIGHT - 3, background);

                String title = trim(review.getTitle(), 27);
                drawString(fontRendererObj, title, left + 5, top + 4, 0xFFFFFF);
                String dateLine = review.getDate() + " | " + trim(review.getStaffName(), 18);
                drawString(fontRendererObj, dateLine, left + 5, top + 18,
                        overdue ? 0xFFFF6666 : 0xFFBBBBBB);
                if (overdue) drawString(fontRendererObj, "OVERDUE", left + columnWidth - 52, top + 18, 0xFFFF5555);
            }
        }

        if (calendar.getManager().size() == 0) {
            drawCenteredString(fontRendererObj, "No staff reviews yet. Add a review to populate the board.",
                    width / 2, height / 2, 0xAAAAAA);
        } else if (getSelectedReview() != null) {
            StaffReview selected = getSelectedReview();
            drawString(fontRendererObj, "Selected: " + trim(selected.getTitle(), 52)
                    + " | " + selected.getStaffName() + " | Use Next status to move it between columns.",
                    12, height - 42, 0xFFCCCCCC);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
