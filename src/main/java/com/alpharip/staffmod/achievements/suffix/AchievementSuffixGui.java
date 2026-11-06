package com.alpharip.staffmod.achievements.suffix;

import java.io.IOException;
import java.util.List;
import com.alpharip.staffmod.achievements.AchievementChecklist;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Minecraft 1.8.9 preview for browsing and selecting achievement-unlocked suffixes. */
public final class AchievementSuffixGui extends GuiScreen {
    private static final int BACK_BUTTON = 1;
    private static final int SELECT_BUTTON = 2;
    private static final int DISABLE_BUTTON = 3;

    private final GuiScreen parent;
    private final AchievementSuffixService service;
    private final AchievementChecklist checklist;
    private List<AchievementSuffixEntry> entries;
    private int selectedIndex;

    public AchievementSuffixGui(GuiScreen parent, AchievementSuffixService service,
                                AchievementChecklist checklist) {
        this.parent = parent;
        this.service = service == null
                ? new AchievementSuffixService(null, null) : service;
        this.checklist = checklist == null ? new AchievementChecklist() : checklist;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(SELECT_BUTTON, width / 2 - 104, height - 30, 100, 20, "Select"));
        buttonList.add(new GuiButton(DISABLE_BUTTON, width / 2 + 4, height - 30, 100, 20, "Disable"));
        buttonList.add(new GuiButton(BACK_BUTTON, width - 86, 8, 78, 20, "Back"));
        refreshEntries();
    }

    private void refreshEntries() {
        entries = service.getRegistry().getAll();
        if (entries.isEmpty()) selectedIndex = 0;
        else selectedIndex = Math.max(0, Math.min(selectedIndex, entries.size() - 1));
        updateButtons();
    }

    private AchievementSuffixEntry getSelected() {
        return entries == null || entries.isEmpty() ? null : entries.get(selectedIndex);
    }

    private void updateButtons() {
        AchievementSuffixEntry selected = getSelected();
        GuiButton select = findButton(SELECT_BUTTON);
        if (select != null) select.enabled = service.isUnlocked(selected, checklist);
        GuiButton disable = findButton(DISABLE_BUTTON);
        if (disable != null) disable.enabled = service.getSettings().isEnabled();
    }

    private GuiButton findButton(int id) {
        for (GuiButton button : buttonList) if (button.id == id) return button;
        return null;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK_BUTTON) {
            mc.displayGuiScreen(parent);
        } else if (button.id == SELECT_BUTTON) {
            AchievementSuffixEntry selected = getSelected();
            if (selected != null) service.select(selected.getId(), checklist);
        } else if (button.id == DISABLE_BUTTON) {
            service.disable();
        }
        updateButtons();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int left = Math.max(12, width / 2 - 170);
        int top = 48;
        for (int i = 0; i < entries.size(); i++) {
            int y = top + i * 22;
            if (mouseX >= left && mouseX <= width - left
                    && mouseY >= y - 3 && mouseY <= y + 16) {
                selectedIndex = i;
                updateButtons();
                break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = Math.max(12, width / 2 - 170);
        int right = Math.min(width - 12, width / 2 + 170);
        drawRect(left, 28, right, height - 58, 0xF00B0D12);
        drawRect(left, 28, right, 30, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ACHIEVEMENT SUFFIXES", width / 2, 12, 0xFFF2F3F5);

        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No suffix definitions have been supplied.", width / 2,
                    height / 2 - 4, 0xFF858D9B);
        } else {
            int visible = Math.max(1, (height - 118) / 22);
            int first = Math.max(0, Math.min(selectedIndex, entries.size() - visible));
            int end = Math.min(entries.size(), first + visible);
            for (int i = first; i < end; i++) {
                AchievementSuffixEntry entry = entries.get(i);
                int y = 48 + (i - first) * 22;
                boolean unlocked = service.isUnlocked(entry, checklist);
                if (i == selectedIndex) drawRect(left + 5, y - 3, right - 5, y + 16, 0x80343A45);
                String state = unlocked ? "UNLOCKED" : "LOCKED";
                drawString(fontRendererObj, entry.getDisplayText(), left + 12, y, 0xFFE0E3E8);
                int stateColor = unlocked ? 0xFFFFD866 : 0xFF858D9B;
                drawString(fontRendererObj, state, right - fontRendererObj.getStringWidth(state) - 12,
                        y, stateColor);
                if (i == selectedIndex) {
                    String requirement = "Requires: " + entry.getRequiredAchievementId();
                    drawString(fontRendererObj, requirement, left + 12, y + 10, 0xFF9AA2AF);
                }
            }
        }

        AchievementSuffixEntry active = service.getActiveSuffix(checklist);
        String activeText = active == null ? "Active suffix: None" : "Active suffix: " + active.getDisplayText();
        drawCenteredString(fontRendererObj, activeText, width / 2, height - 48, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
