// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quest Book presentation for Pixel Garden.
 *
 * This screen deliberately only displays the persisted quest state. Quest acceptance,
 * delivery transactions and reward granting are connected when those gameplay flows exist.
 */
public final class PixelGardenQuestBookGUI extends GuiScreen {
    private static final int TAB_ACTIVE = 10;
    private static final int TAB_AVAILABLE = 11;
    private static final int TAB_COMPLETED = 12;
    private static final int QUEST_BASE = 100;
    private static final int BACK = 4;

    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();
    private final PixelGardenProgressionData progression = game.getProgressionData();
    private PixelGardenProgressionData.QuestStatus viewStatus = PixelGardenProgressionData.QuestStatus.ACTIVE;
    private final List<String> visibleInstanceIds = new ArrayList<String>();
    private String selectedInstanceId;
    private int scrollOffset;

    public PixelGardenQuestBookGUI(GuiScreen parent) {
        this.parent = parent;
    }

    private List<PixelGardenProgressionData.QuestInstance> questsForView() {
        List<PixelGardenProgressionData.QuestInstance> result =
                new ArrayList<PixelGardenProgressionData.QuestInstance>();
        for (PixelGardenProgressionData.QuestInstance instance : progression.getInstances()) {
            if (instance.getStatus() == viewStatus) result.add(instance);
        }
        return result;
    }

    private PixelGardenProgressionData.QuestInstance selectedQuest() {
        if (selectedInstanceId == null) return null;
        for (PixelGardenProgressionData.QuestInstance instance : progression.getInstances()) {
            if (selectedInstanceId.equals(instance.instanceId)) return instance;
        }
        return null;
    }

    private PixelGardenProgressionData.QuestDefinition definition(
            PixelGardenProgressionData.QuestInstance instance) {
        return instance == null ? null : progression.getDefinition(instance.definitionId);
    }

    private int listLeft() { return Math.max(16, width / 2 - 280); }
    private int bookTop() { return 62; }
    private int bookWidth() { return Math.min(560, width - 32); }
    private int bookHeight() { return Math.max(230, height - 108); }
    private int rowHeight() { return 28; }
    private int visibleRows() { return Math.max(1, (bookHeight() - 112) / rowHeight()); }

    @Override
    public void initGui() {
        buttonList.clear();
        visibleInstanceIds.clear();

        int left = listLeft();
        int top = bookTop();
        int bookWidth = bookWidth();
        int bookHeight = bookHeight();
        int tabY = top + 34;
        int tabGap = 5;
        int tabWidth = Math.max(64, (bookWidth - 34 - tabGap * 2) / 3);
        buttonList.add(new AlphaButton(TAB_ACTIVE, left + 12, tabY, tabWidth, 20,
                "Active (" + count(PixelGardenProgressionData.QuestStatus.ACTIVE) + ")",
                viewStatus == PixelGardenProgressionData.QuestStatus.ACTIVE));
        buttonList.add(new AlphaButton(TAB_AVAILABLE, left + 12 + tabWidth + tabGap, tabY,
                tabWidth, 20, "Available (" + count(PixelGardenProgressionData.QuestStatus.AVAILABLE) + ")",
                viewStatus == PixelGardenProgressionData.QuestStatus.AVAILABLE));
        buttonList.add(new AlphaButton(TAB_COMPLETED, left + 12 + (tabWidth + tabGap) * 2, tabY,
                tabWidth, 20, "Completed (" + count(PixelGardenProgressionData.QuestStatus.COMPLETED) + ")",
                viewStatus == PixelGardenProgressionData.QuestStatus.COMPLETED));

        List<PixelGardenProgressionData.QuestInstance> quests = questsForView();
        int rows = visibleRows();
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, quests.size() - rows)));
        int listWidth = Math.max(130, bookWidth / 2 - 22);
        int rowX = left + 12;
        int rowY = top + 68;
        int end = Math.min(quests.size(), scrollOffset + rows);
        for (int i = scrollOffset; i < end; i++) {
            PixelGardenProgressionData.QuestInstance instance = quests.get(i);
            PixelGardenProgressionData.QuestDefinition def = definition(instance);
            if (def == null) continue;
            int buttonId = QUEST_BASE + visibleInstanceIds.size();
            visibleInstanceIds.add(instance.instanceId);
            String prefix = instance.instanceId.equals(selectedInstanceId) ? "> " : "";
            String label = prefix + def.name;
            if (label.length() > 23) label = label.substring(0, 20) + "...";
            buttonList.add(new AlphaButton(buttonId, rowX, rowY + (i - scrollOffset) * rowHeight(),
                    listWidth, 24, label, instance.instanceId.equals(selectedInstanceId)));
        }

        buttonList.add(new AlphaButton(BACK, width / 2 - 62, height - 34, 124, 22, "Close Book"));
    }

    private int count(PixelGardenProgressionData.QuestStatus status) {
        int result = 0;
        for (PixelGardenProgressionData.QuestInstance instance : progression.getInstances()) {
            if (instance.getStatus() == status) result++;
        }
        return result;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            List<PixelGardenProgressionData.QuestInstance> quests = questsForView();
            int max = Math.max(0, quests.size() - visibleRows());
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel < 0 ? 1 : -1)));
            initGui();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            if (mc != null) mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == TAB_ACTIVE || button.id == TAB_AVAILABLE || button.id == TAB_COMPLETED) {
            viewStatus = button.id == TAB_ACTIVE ? PixelGardenProgressionData.QuestStatus.ACTIVE
                    : button.id == TAB_AVAILABLE ? PixelGardenProgressionData.QuestStatus.AVAILABLE
                    : PixelGardenProgressionData.QuestStatus.COMPLETED;
            selectedInstanceId = null;
            scrollOffset = 0;
            initGui();
            return;
        }
        int index = button.id - QUEST_BASE;
        if (index >= 0 && index < visibleInstanceIds.size()) {
            selectedInstanceId = visibleInstanceIds.get(index);
            initGui();
        }
    }

    private void drawBookPage(int x, int y, int w, int h) {
        // Leather cover and gilded page edges.
        drawRect(x - 5, y - 4, x + w + 5, y + h + 4, 0xFF21170F);
        drawRect(x - 3, y - 2, x + w + 3, y + h + 2, 0xFF8E622D);
        drawRect(x, y, x + w, y + h, 0xFFF0E2BD);
        drawRect(x + 4, y + 4, x + w - 4, y + h - 4, 0xFFDFCCA0);
        drawRect(x + w / 2 - 2, y + 2, x + w / 2 + 2, y + h - 2, 0xFF9D7847);
        drawRect(x + w / 2 - 1, y + 2, x + w / 2 + 1, y + h - 2, 0xFFB99860);
        drawRect(x + 8, y + 8, x + w - 8, y + 9, 0xFFB99B66);
        drawRect(x + 8, y + h - 9, x + w - 8, y + h - 8, 0xFFB99B66);
    }

    private void drawWrapped(String text, int x, int y, int maxWidth, int color, int lineHeight) {
        if (text == null || text.trim().isEmpty()) {
            drawString(fontRendererObj, "No description has been added yet.", x, y, color);
            return;
        }
        String[] words = text.split("\\s+");
        String line = "";
        int drawY = y;
        for (String word : words) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (fontRendererObj.getStringWidth(next) > maxWidth && !line.isEmpty()) {
                drawString(fontRendererObj, line, x, drawY, color);
                drawY += lineHeight;
                line = word;
                if (drawY > y + lineHeight * 4) break;
            } else line = next;
        }
        if (!line.isEmpty() && drawY <= y + lineHeight * 4) drawString(fontRendererObj, line, x, drawY, color);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, 48, 0xF00B1018);
        drawRect(0, 46, width, 49, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "PIXEL GARDEN", width / 2, 8, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "QUEST JOURNAL  •  RECORDS OF YOUR GARDEN", width / 2, 25, 0xFFE3E8F0);

        int left = listLeft();
        int top = bookTop();
        int w = bookWidth();
        int h = bookHeight();
        drawBookPage(left, top, w, h);
        drawCenteredString(fontRendererObj, "QUESTS", left + w / 4, top + 14, 0xFF67451F);
        drawCenteredString(fontRendererObj, "FIELD NOTES", left + (w * 3) / 4, top + 14, 0xFF67451F);

        int listX = left + 12;
        int listY = top + 65;
        int listW = Math.max(130, w / 2 - 22);
        int detailX = left + w / 2 + 12;
        int detailW = w / 2 - 26;

        List<PixelGardenProgressionData.QuestInstance> quests = questsForView();
        if (quests.isEmpty()) {
            String empty = viewStatus == PixelGardenProgressionData.QuestStatus.ACTIVE
                    ? "No active quests yet."
                    : viewStatus == PixelGardenProgressionData.QuestStatus.AVAILABLE
                    ? "No available quests right now." : "No completed quests yet.";
            drawCenteredString(fontRendererObj, empty, listX + listW / 2, listY + 36, 0xFF7D6544);
            drawCenteredString(fontRendererObj, "Your next chapter will be written here.", listX + listW / 2,
                    listY + 50, 0xFF8A7657);
        } else {
            int rows = visibleRows();
            int end = Math.min(quests.size(), scrollOffset + rows);
            for (int i = scrollOffset; i < end; i++) {
                PixelGardenProgressionData.QuestInstance instance = quests.get(i);
                PixelGardenProgressionData.QuestDefinition def = definition(instance);
                if (def == null) continue;
                int rowY = listY + (i - scrollOffset) * rowHeight();
                boolean selected = instance.instanceId.equals(selectedInstanceId);
                drawRect(listX, rowY, listX + listW, rowY + 24,
                        selected ? 0xFFBBA06B : 0xFFCFB987);
                drawRect(listX, rowY, listX + 3, rowY + 24, selected ? 0xFF9B6125 : 0xFFB49A6B);
                String name = def.name;
                if (fontRendererObj.getStringWidth(name) > listW - 16) {
                    while (!name.isEmpty() && fontRendererObj.getStringWidth(name + "...") > listW - 16)
                        name = name.substring(0, name.length() - 1);
                    name += "...";
                }
                drawString(fontRendererObj, name, listX + 8, rowY + 3, 0xFF342819);
                drawString(fontRendererObj, def.tier.isEmpty() ? statusLabel(instance.getStatus()) : def.tier,
                        listX + 8, rowY + 14, 0xFF725532);
            }
        }

        PixelGardenProgressionData.QuestInstance selected = selectedQuest();
        PixelGardenProgressionData.QuestDefinition def = definition(selected);
        if (selected == null || def == null || selected.getStatus() != viewStatus) {
            drawCenteredString(fontRendererObj, "Choose a quest", detailX + detailW / 2, listY + 35, 0xFF67451F);
            drawWrapped("Select an entry on the left to read its story, objectives and rewards.",
                    detailX + 8, listY + 54, detailW - 16, 0xFF705A3D, 11);
            drawString(fontRendererObj, "GARDEN LEVEL  " + progression.getLevel(),
                    detailX + 8, top + h - 35, 0xFF67451F);
        } else {
            int y = listY + 2;
            drawWrapped(def.name, detailX + 8, y, detailW - 16, 0xFF493019, 12);
            y += 30;
            drawRect(detailX + 8, y, detailX + detailW - 8, y + 1, 0xFFB49A6B);
            y += 9;
            drawWrapped(def.description, detailX + 8, y, detailW - 16, 0xFF5D4A31, 11);
            y += 62;
            drawString(fontRendererObj, "REQUIREMENTS", detailX + 8, y, 0xFF8A5A20);
            y += 13;
            drawString(fontRendererObj, "Level " + def.requiredLevel + "  |  Prestige " + def.requiredPrestige,
                    detailX + 8, y, 0xFF493019);
            y += 19;
            drawString(fontRendererObj, "OBJECTIVES", detailX + 8, y, 0xFF8A5A20);
            y += 13;
            if (def.objectives.isEmpty()) {
                drawString(fontRendererObj, "Objectives not configured.", detailX + 8, y, 0xFF705A3D);
            } else {
                int shown = 0;
                for (PixelGardenProgressionData.Objective objective : def.objectives) {
                    if (shown >= 4 || y > top + h - 68) break;
                    int progress = selected.getObjectiveProgress(objective.id);
                    drawString(fontRendererObj, objective.target + "  " + progress + "/" + objective.requiredAmount,
                            detailX + 8, y, 0xFF493019);
                    int barW = Math.max(20, detailW - 20);
                    drawRect(detailX + 8, y + 10, detailX + 8 + barW, y + 14, 0xFFBCA77D);
                    int fill = (int) ((long) barW * Math.max(0, Math.min(objective.requiredAmount, progress))
                            / objective.requiredAmount);
                    if (fill > 0) drawRect(detailX + 8, y + 10, detailX + 8 + fill, y + 14, 0xFF668447);
                    y += 23;
                    shown++;
                }
            }
            int rewardY = top + h - 52;
            drawString(fontRendererObj, "REWARDS", detailX + 8, rewardY, 0xFF8A5A20);
            String rewardText = def.rewards.isEmpty() ? "Not configured"
                    : def.rewards.toString().replace("{", "").replace("}", "").replace("=", " x ");
            drawWrapped(rewardText, detailX + 8, rewardY + 12, detailW - 16, 0xFF493019, 11);
            drawString(fontRendererObj, statusLabel(selected.getStatus()), detailX + 8, top + h - 20,
                    selected.getStatus() == PixelGardenProgressionData.QuestStatus.COMPLETED
                            ? 0xFF4B6B34 : 0xFF8A5A20);
        }

        if (quests.size() > visibleRows()) {
            drawCenteredString(fontRendererObj, "Scroll to browse  " + (scrollOffset + 1) + "-"
                    + Math.min(quests.size(), scrollOffset + visibleRows()) + "/" + quests.size(),
                    listX + listW / 2, top + h - 24, 0xFF7D6544);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String statusLabel(PixelGardenProgressionData.QuestStatus status) {
        switch (status) {
            case ACTIVE: return "IN PROGRESS";
            case AVAILABLE: return "AVAILABLE";
            case COMPLETED: return "COMPLETED";
            default: return "ABANDONED";
        }
    }
}
