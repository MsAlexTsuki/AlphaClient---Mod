package com.alpharip.staffmod.achievements;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Minecraft 1.8.9 profile-wall preview with paging, sorting, and locked-item visibility. */
public final class AchievementWallGui extends GuiScreen {
    private static final int BACK = 1, PREVIOUS = 2, NEXT = 3, SORT = 4, LOCKED = 5;
    private static final int PAGE_SIZE = 6;
    private final GuiScreen parent;
    private final AchievementWall wall;
    private int page;

    public AchievementWallGui(GuiScreen parent, AchievementWall wall) {
        this.parent = parent;
        this.wall = wall == null ? new AchievementWall() : wall;
    }

    @Override public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(BACK, width / 2 - 42, height - 28, 84, 20, "Back"));
        buttonList.add(new GuiButton(PREVIOUS, width / 2 - 132, height - 28, 40, 20, "<"));
        buttonList.add(new GuiButton(NEXT, width / 2 + 92, height - 28, 40, 20, ">"));
        buttonList.add(new GuiButton(SORT, width / 2 - 180, height - 28, 44, 20, "Sort"));
        buttonList.add(new GuiButton(LOCKED, width / 2 + 136, height - 28, 64, 20, "Show locked"));
        updateButtons();
    }

    private void updateButtons() {
        AchievementWallSnapshot snapshot = wall.createSnapshot();
        int pages = Math.max(1, (snapshot.getEntries().size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pages - 1));
        for (GuiButton button : buttonList) {
            if (button.id == PREVIOUS) button.enabled = page > 0;
            if (button.id == NEXT) button.enabled = page + 1 < pages;
            if (button.id == SORT) button.displayString = "Sort: " + shortSort(wall.getSettings().getSort());
            if (button.id == LOCKED) button.displayString =
                    wall.getSettings().isShowLocked() ? "Hide locked" : "Show locked";
        }
    }

    private String shortSort(AchievementWallSort sort) {
        if (sort == AchievementWallSort.OLDEST_FIRST) return "Old";
        if (sort == AchievementWallSort.TITLE) return "A-Z";
        if (sort == AchievementWallSort.CATEGORY) return "Group";
        return "New";
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (button.id == PREVIOUS && page > 0) page--;
        if (button.id == NEXT) page++;
        if (button.id == SORT) {
            AchievementWallSort[] modes = AchievementWallSort.values();
            AchievementWallSort current = wall.getSettings().getSort();
            wall.getSettings().setSort(modes[(current.ordinal() + 1) % modes.length]);
        }
        if (button.id == LOCKED) wall.getSettings().setShowLocked(!wall.getSettings().isShowLocked());
        updateButtons();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = Math.max(12, width / 2 - 190), right = Math.min(width - 12, width / 2 + 190);
        int top = Math.max(20, height / 2 - 112), bottom = Math.min(height - 38, top + 218);
        AchievementWallSnapshot snapshot = wall.createSnapshot();
        drawRect(left, top, right, bottom, 0xF00B0D12);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ACHIEVEMENT WALL", width / 2, top + 10, 0xFFF2F3F5);
        drawCenteredString(fontRendererObj, "Unlocked: " + snapshot.getUnlockedCount() + " / "
                + snapshot.getTotalCount() + "  (" + snapshot.getCompletionPercent() + "%)",
                width / 2, top + 28, 0xFFB8BDC8);

        if (!wall.getSettings().isEnabled()) {
            drawCenteredString(fontRendererObj, "Achievement wall is disabled.", width / 2, top + 90, 0xFF858D9B);
        } else if (snapshot.getEntries().isEmpty()) {
            drawCenteredString(fontRendererObj, "No achievements to display.", width / 2, top + 90, 0xFF858D9B);
        } else {
            List<AchievementWallEntry> entries = snapshot.getEntries();
            int start = page * PAGE_SIZE, end = Math.min(entries.size(), start + PAGE_SIZE);
            for (int i = start; i < end; i++) {
                AchievementWallEntry entry = entries.get(i);
                int y = top + 48 + (i - start) * 25;
                drawRect(left + 10, y - 3, right - 10, y + 19, 0x80343A45);
                String state = entry.isUnlocked() ? "UNLOCKED" : entry.getProgressPercent() + "%";
                drawString(fontRendererObj, entry.getTitle(), left + 18, y + 1, 0xFFE0E3E8);
                drawString(fontRendererObj, entry.getCategory(), left + 18, y + 11, 0xFF858D9B);
                drawString(fontRendererObj, state, right - fontRendererObj.getStringWidth(state) - 18,
                        y + 5, entry.isUnlocked() ? 0xFFFFD866 : 0xFF858D9B);
            }
            int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
            drawCenteredString(fontRendererObj, "Page " + (page + 1) + " / " + pages,
                    width / 2, bottom - 15, 0xFF858D9B);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
