package com.alpharip.staffmod.achievements;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Minecraft 1.8.9 checklist preview with selectable entries and explanation details. */
public final class AchievementChecklistGui extends GuiScreen {
    private static final int BACK_BUTTON = 1;
    private static final int PREVIOUS_BUTTON = 2;
    private static final int NEXT_BUTTON = 3;
    private final GuiScreen parent;
    private final AchievementChecklist checklist;
    private final AchievementChecklistSettings settings;
    private List<AchievementEntry> visibleEntries = Collections.emptyList();
    private int selectedIndex;

    public AchievementChecklistGui(GuiScreen parent, AchievementChecklist checklist,
                                   AchievementChecklistSettings settings) {
        this.parent = parent;
        this.checklist = checklist == null ? new AchievementChecklist() : checklist;
        this.settings = settings == null ? new AchievementChecklistSettings() : settings;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(BACK_BUTTON, width / 2 - 42, height - 28, 84, 20, "Back"));
        buttonList.add(new GuiButton(PREVIOUS_BUTTON, width / 2 - 100, height - 28, 50, 20, "<"));
        buttonList.add(new GuiButton(NEXT_BUTTON, width / 2 + 50, height - 28, 50, 20, ">"));
        refreshEntries();
    }

    private void refreshEntries() {
        visibleEntries = settings.isEnabled()
                ? AchievementChecklistSearch.search(checklist.getEntries(), settings.getSearchQuery())
                : Collections.<AchievementEntry>emptyList();
        if (visibleEntries.isEmpty()) selectedIndex = 0;
        else selectedIndex = Math.max(0, Math.min(selectedIndex, visibleEntries.size() - 1));
        updateNavigation();
    }

    private void updateNavigation() {
        for (GuiButton button : buttonList) {
            if (button.id == PREVIOUS_BUTTON) button.enabled = selectedIndex > 0;
            if (button.id == NEXT_BUTTON) button.enabled = selectedIndex < visibleEntries.size() - 1;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK_BUTTON) mc.displayGuiScreen(parent);
        else if (button.id == PREVIOUS_BUTTON && selectedIndex > 0) selectedIndex--;
        else if (button.id == NEXT_BUTTON && selectedIndex + 1 < visibleEntries.size()) selectedIndex++;
        updateNavigation();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int left = Math.max(12, width / 2 - 190);
        int top = Math.max(34, height / 2 - 105);
        int rowTop = top + 48;
        for (int i = 0; i < visibleEntries.size() && i < 6; i++) {
            int y = rowTop + i * 18;
            if (mouseX >= left + 10 && mouseX <= width / 2 + 180 && mouseY >= y - 2 && mouseY <= y + 13) {
                selectedIndex = i;
                updateNavigation();
                break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = Math.max(12, width / 2 - 190);
        int top = Math.max(24, height / 2 - 112);
        int right = Math.min(width - 12, width / 2 + 190);
        int bottom = Math.min(height - 38, top + 218);
        drawRect(left, top, right, bottom, 0xF00B0D12);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ACHIEVEMENTS", width / 2, top + 10, 0xFFF2F3F5);
        drawString(fontRendererObj, "Completed: " + checklist.getCompletedCount() + " / " + checklist.getTotalCount(),
                left + 12, top + 29, 0xFFB8BDC8);

        if (!settings.isEnabled()) {
            drawCenteredString(fontRendererObj, "Achievement checklist is disabled.", width / 2, top + 82, 0xFF858D9B);
        } else if (visibleEntries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No achievements match this view.", width / 2, top + 82, 0xFF858D9B);
        } else {
            int listBottom = Math.min(visibleEntries.size(), 6);
            for (int i = 0; i < listBottom; i++) {
                AchievementEntry entry = visibleEntries.get(i);
                int y = top + 48 + i * 18;
                if (i == selectedIndex) drawRect(left + 8, y - 3, right - 8, y + 13, 0x80343A45);
                String state = entry.isUnlocked() ? "UNLOCKED" : entry.getProgressPercent() + "%";
                drawString(fontRendererObj, entry.getTitle(), left + 13, y, 0xFFE0E3E8);
                drawString(fontRendererObj, state, right - fontRendererObj.getStringWidth(state) - 13, y,
                        entry.isUnlocked() ? 0xFFFFD866 : 0xFF858D9B);
            }
            AchievementEntry selected = visibleEntries.get(selectedIndex);
            int detailY = top + 166;
            drawString(fontRendererObj, selected.getCategory() + "  •  " + selected.getDescription(),
                    left + 12, detailY, 0xFFB8BDC8);
            drawString(fontRendererObj, "How to unlock:", left + 12, detailY + 15, 0xFFFFD866);
            drawString(fontRendererObj, selected.getExplanation(), left + 12, detailY + 28, 0xFFE0E3E8);
            if (!selected.isUnlocked()) {
                drawString(fontRendererObj, "Progress: " + selected.getProgress() + " / " + selected.getTarget(),
                        left + 12, detailY + 42, 0xFF858D9B);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
