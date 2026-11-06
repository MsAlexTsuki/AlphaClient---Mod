package com.alpharip.staffdashboardexpansion.shifts;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft 1.8.9 GUI for the local shift-start checklist prototype.
 *
 * The host module must open this screen. Staff authentication, dashboard
 * registration, and cross-session synchronization are intentionally not wired.
 */
public final class ShiftStartChecklistGui extends GuiScreen {
    private final ShiftStartChecklist checklist;
    private ChecklistCategory selectedCategory;
    private List<ChecklistItem> visibleItems = new ArrayList<ChecklistItem>();
    private int scrollOffset;

    public ShiftStartChecklistGui(ShiftStartChecklist checklist) {
        if (checklist == null) {
            throw new IllegalArgumentException("checklist must not be null.");
        }
        this.checklist = checklist;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int y = height - 30;
        buttonList.add(new GuiButton(0, center - 154, y, 100, 20, "All"));
        buttonList.add(new GuiButton(1, center - 50, y, 100, 20, "Reset"));
        buttonList.add(new GuiButton(2, center + 54, y, 100, 20, "Refresh"));
        refreshItems();
    }

    private void refreshItems() {
        visibleItems = selectedCategory == null
            ? checklist.getItems() : checklist.getItems(selectedCategory);
        int maxOffset = Math.max(0, visibleItems.size() - getVisibleRowCount());
        scrollOffset = Math.min(scrollOffset, maxOffset);
    }

    private int getVisibleRowCount() {
        return Math.max(1, (height - 112) / 32);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            selectedCategory = null;
            scrollOffset = 0;
        } else if (button.id == 1) {
            checklist.reset();
        }
        refreshItems();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 && mouseButton != 1) {
            return;
        }

        int categoryY = 42;
        int categoryIndex = 0;
        for (ChecklistCategory category : ChecklistCategory.values()) {
            int left = width / 2 - 190 + categoryIndex * 96;
            if (mouseY >= categoryY && mouseY < categoryY + 20
                    && mouseX >= left && mouseX <= left + 92) {
                selectedCategory = category;
                scrollOffset = 0;
                refreshItems();
                return;
            }
            categoryIndex++;
        }

        int firstRowY = 82;
        int rowHeight = 32;
        for (int i = 0; i < getVisibleRowCount(); i++) {
            int index = scrollOffset + i;
            if (index >= visibleItems.size()) {
                break;
            }
            int rowY = firstRowY + i * rowHeight;
            if (mouseY >= rowY && mouseY < rowY + 28
                    && mouseX >= width / 2 - 190 && mouseX <= width / 2 + 190) {
                ChecklistItem item = visibleItems.get(index);
                if (mouseButton == 1) {
                    checklist.remove(item.getId());
                } else {
                    checklist.setCompleted(item.getId(), !item.isCompleted());
                }
                refreshItems();
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 200) {
            scrollOffset = Math.max(0, scrollOffset - 1);
            return;
        }
        if (keyCode == 208) {
            int maxOffset = Math.max(0, visibleItems.size() - getVisibleRowCount());
            scrollOffset = Math.min(maxOffset, scrollOffset + 1);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Shift Start Checklist",
            width / 2, 14, 0xFFFFFF);

        String progress = checklist.getCompletedCount() + " / "
            + checklist.getTotalCount() + " completed  |  "
            + checklist.getRemainingCount() + " remaining";
        drawCenteredString(fontRendererObj, progress, width / 2, 28, 0xFFCCCCCC);

        int categoryY = 42;
        int categoryIndex = 0;
        for (ChecklistCategory category : ChecklistCategory.values()) {
            int left = width / 2 - 190 + categoryIndex * 96;
            boolean selected = selectedCategory == category;
            drawRect(left, categoryY, left + 92, categoryY + 20,
                selected ? 0xAA806020 : 0xAA303030);
            drawCenteredString(fontRendererObj, category.getDisplayName(),
                left + 46, categoryY + 6, 0xFFFFFFFF);
            categoryIndex++;
        }

        int firstRowY = 82;
        int rowHeight = 32;
        int rows = getVisibleRowCount();
        for (int i = 0; i < rows; i++) {
            int index = scrollOffset + i;
            if (index >= visibleItems.size()) {
                break;
            }
            ChecklistItem item = visibleItems.get(index);
            int rowY = firstRowY + i * rowHeight;
            drawRect(width / 2 - 190, rowY, width / 2 + 190, rowY + 28,
                item.isCompleted() ? 0x88405A35 : 0x88303030);
            String marker = item.isCompleted() ? "[x] " : "[ ] ";
            drawString(fontRendererObj, marker + trim(item.getTitle(), 42),
                width / 2 - 180, rowY + 5, item.isCompleted() ? 0xFFB8E6A0 : 0xFFFFFFFF);
            if (!item.getDescription().isEmpty()) {
                drawString(fontRendererObj, trim(item.getDescription(), 66),
                    width / 2 - 180, rowY + 16, 0xFFAAAAAA);
            }
        }

        if (visibleItems.isEmpty()) {
            drawCenteredString(fontRendererObj, "No checklist items in this category.",
                width / 2, height / 2, 0xFFAAAAAA);
        }

        String hint = "Left-click: toggle  |  Right-click: remove  |  Up/Down: scroll";
        drawCenteredString(fontRendererObj, hint, width / 2, height - 46, 0xFFAAAAAA);
        if (checklist.isComplete()) {
            drawCenteredString(fontRendererObj, "Shift checklist complete",
                width / 2, height - 60, 0xFF9BE28A);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
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
