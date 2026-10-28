// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quest Book interface for Pixel Garden quest selection, crop delivery and rewards.
 */
public final class PixelGardenQuestBookGUI extends GuiScreen {
    private static final int TAB_ACTIVE = 10;
    private static final int TAB_AVAILABLE = 11;
    private static final int TAB_COMPLETED = 12;
    private static final int QUEST_BASE = 100;
    private static final int BACK = 4;
    private static final int ACCEPT = 20;
    private static final int SUBMIT = 21;
    private static final int CLAIM = 22;
    private static final int AMOUNT_DOWN = 23;
    private static final int AMOUNT_UP = 24;
    private static final int AMOUNT_DOWN_10 = 25;
    private static final int AMOUNT_DOWN_100 = 26;
    private static final int AMOUNT_UP_10 = 27;
    private static final int AMOUNT_UP_100 = 28;
    private static final int CROP_PREV = 29;
    private static final int CROP_NEXT = 30;

    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();
    private final PixelGardenProgressionData progression = game.getProgressionData();
    private PixelGardenProgressionData.QuestStatus viewStatus = PixelGardenProgressionData.QuestStatus.ACTIVE;
    private final List<String> visibleInstanceIds = new ArrayList<String>();
    private String selectedInstanceId;
    private int scrollOffset;
    private int selectedDeliveryAmount = 1;
    private int selectedDeliveryCropIndex;
    private String actionMessage = "";

    public PixelGardenQuestBookGUI(GuiScreen parent) {
        this.parent = parent;
        progression.ensureDefaultCatalog();
        try { progression.mergeSharedDefinitions(SupabaseCommunityService.getSharedPixelGardenQuests()); }
        catch (Exception exception) { System.err.println("[AlphaClient] Could not sync shared Pixel Garden quests: " + exception.getMessage()); }
    }

    private List<PixelGardenProgressionData.QuestInstance> questsForView() {
        List<PixelGardenProgressionData.QuestInstance> result =
                new ArrayList<PixelGardenProgressionData.QuestInstance>();
        for (PixelGardenProgressionData.QuestInstance instance : progression.getInstances()) {
            if (instance.getStatus() == viewStatus) result.add(instance);
        }
        // Keep the tutorial sequence ahead of quests introduced through NPCs or other systems.
        java.util.Collections.sort(result, new java.util.Comparator<PixelGardenProgressionData.QuestInstance>() {
            private int tutorialOrder(PixelGardenProgressionData.QuestInstance instance) {
                if ("pg_fresh_start".equals(instance.definitionId)) return 0;
                if ("pg_first_coins".equals(instance.definitionId)) return 1;
                if ("pg_growing_bigger".equals(instance.definitionId)) return 2;
                if ("pg_your_own_garden".equals(instance.definitionId)) return 3;
                return 100;
            }
            @Override public int compare(PixelGardenProgressionData.QuestInstance left,
                    PixelGardenProgressionData.QuestInstance right) {
                return Integer.compare(tutorialOrder(left), tutorialOrder(right));
            }
        });
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

        PixelGardenProgressionData.QuestInstance selected = selectedQuest();
        PixelGardenProgressionData.QuestDefinition selectedDefinition = definition(selected);
        int actionY = height - 61;
        int actionX = left + bookWidth / 2 + 12;
        int actionWidth = Math.max(90, bookWidth / 2 - 26);
        if (selected != null && selectedDefinition != null
                && selected.getStatus() == PixelGardenProgressionData.QuestStatus.AVAILABLE) {
            buttonList.add(new AlphaButton(ACCEPT, actionX, actionY, actionWidth, 20, "Accept Quest"));
        } else if (selected != null && selectedDefinition != null
                && selected.getStatus() == PixelGardenProgressionData.QuestStatus.ACTIVE) {
            PixelGardenProgressionData.Objective delivery = firstDeliveryObjective(selectedDefinition);
            if (delivery != null && selected.getObjectiveProgress(delivery.id) < delivery.requiredAmount) {
                if ("*".equals(delivery.target)) {
                    PixelGardenGame.Crop[] crops = PixelGardenGame.Crop.values();
                    PixelGardenGame.Crop selectedCrop = crops[Math.floorMod(selectedDeliveryCropIndex, crops.length)];
                    PixelGardenGame.Crop nextCrop = crops[Math.floorMod(selectedDeliveryCropIndex + 1, crops.length)];
                    buttonList.add(new AlphaButton(CROP_PREV, actionX + actionWidth / 2 - 92, actionY - 22, 88, 18,
                            "< " + selectedCrop.displayName));
                    buttonList.add(new AlphaButton(CROP_NEXT, actionX + actionWidth / 2 + 4, actionY - 22, 88, 18,
                            nextCrop.displayName + " >"));
                }
                buttonList.add(new AlphaButton(AMOUNT_DOWN, actionX, actionY, 24, 20, "-1"));
                buttonList.add(new AlphaButton(AMOUNT_DOWN_10, actionX + 25, actionY, 30, 20, "-10"));
                buttonList.add(new AlphaButton(AMOUNT_DOWN_100, actionX + 56, actionY, 36, 20, "-100"));
                buttonList.add(new AlphaButton(AMOUNT_UP, actionX + 93, actionY, 24, 20, "+1"));
                buttonList.add(new AlphaButton(AMOUNT_UP_10, actionX + 118, actionY, 30, 20, "+10"));
                buttonList.add(new AlphaButton(AMOUNT_UP_100, actionX + 149, actionY, 36, 20, "+100"));
                buttonList.add(new AlphaButton(SUBMIT, actionX + 186, actionY,
                        Math.max(50, actionWidth - 186), 20, "Submit"));
            }
        } else if (selected != null && selectedDefinition != null
                && selected.getStatus() == PixelGardenProgressionData.QuestStatus.COMPLETED
                && !selected.areRewardsClaimed() && !selectedDefinition.rewards.isEmpty()) {
            buttonList.add(new AlphaButton(CLAIM, actionX, actionY, actionWidth, 20, "Claim Rewards"));
        }
        buttonList.add(new AlphaButton(BACK, width / 2 - 62, height - 34, 124, 22, "Close Book"));
    }

    private PixelGardenProgressionData.Objective firstDeliveryObjective(
            PixelGardenProgressionData.QuestDefinition definition) {
        if (definition == null) return null;
        for (PixelGardenProgressionData.Objective objective : definition.objectives) {
            if ("DELIVER_CROP".equalsIgnoreCase(objective.type)) return objective;
        }
        return null;
    }

    private int playerPrestige() {
        if (!SupabaseCommunityService.isSignedIn()) return 0;
        try {
            com.google.gson.JsonObject state = SupabaseCommunityService.getPixelGardenPrestigeState();
            return state.has("prestige_count") ? Math.max(0, state.get("prestige_count").getAsInt()) : 0;
        } catch (Exception ignored) { return 0; }
    }

    private void saveProgressionAsync() {
        if (!SupabaseCommunityService.isSignedIn()) return;
        final com.google.gson.JsonObject snapshot = game.toSaveData();
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    if (SupabaseCommunityService.isSignedIn()) {
                        SupabaseCommunityService.savePixelGardenSave(snapshot);
                    }
                } catch (Exception exception) {
                    System.err.println("[AlphaClient] Pixel Garden quest save failed: " + exception.getMessage());
                }
            }
        }, "AlphaClient-PixelGardenQuestSave");
        thread.setDaemon(true);
        thread.start();
    }

    private void actionMessage(String message) {
        actionMessage = message == null ? "" : message;
        initGui();
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
        if (button.id == CROP_PREV || button.id == CROP_NEXT) {
            int count = PixelGardenGame.Crop.values().length;
            selectedDeliveryCropIndex = Math.floorMod(selectedDeliveryCropIndex + (button.id == CROP_NEXT ? 1 : -1), count);
            initGui();
            return;
        }
        if (button.id == ACCEPT) {
            PixelGardenProgressionData.QuestInstance selected = selectedQuest();
            if (selected == null) { actionMessage("Select a quest first."); return; }
            if (!progression.acceptQuest(selected.instanceId, progression.getLevel(), playerPrestige())) {
                actionMessage(progression.getActiveQuestCount() >= PixelGardenProgressionData.MAX_ACTIVE_QUESTS
                        ? "You can have at most 5 active quests."
                        : "Quest requirements are not met yet.");
            } else {
                viewStatus = PixelGardenProgressionData.QuestStatus.ACTIVE;
                saveProgressionAsync();
                actionMessage("Quest accepted. Your progress is now being tracked.");
            }
            return;
        }
        if (button.id == AMOUNT_DOWN || button.id == AMOUNT_UP
                || button.id == AMOUNT_DOWN_10 || button.id == AMOUNT_DOWN_100
                || button.id == AMOUNT_UP_10 || button.id == AMOUNT_UP_100) {
            int delta = button.id == AMOUNT_UP ? 1 : button.id == AMOUNT_UP_10 ? 10
                    : button.id == AMOUNT_UP_100 ? 100 : button.id == AMOUNT_DOWN ? -1
                    : button.id == AMOUNT_DOWN_10 ? -10 : -100;
            selectedDeliveryAmount = Math.max(1, Math.min(1000, selectedDeliveryAmount + delta));
            initGui();
            return;
        }
        if (button.id == SUBMIT) {
            PixelGardenProgressionData.QuestInstance selected = selectedQuest();
            PixelGardenProgressionData.QuestDefinition def = definition(selected);
            PixelGardenProgressionData.Objective objective = firstDeliveryObjective(def);
            if (selected == null || def == null || objective == null
                    || selected.getStatus() != PixelGardenProgressionData.QuestStatus.ACTIVE) {
                actionMessage("This quest has no active crop delivery.");
                return;
            }
            PixelGardenGame.Crop crop;
            if ("*".equals(objective.target)) {
                PixelGardenGame.Crop[] crops = PixelGardenGame.Crop.values();
                crop = crops[Math.floorMod(selectedDeliveryCropIndex, crops.length)];
            } else {
                try { crop = PixelGardenGame.Crop.valueOf(objective.target.toUpperCase(java.util.Locale.ROOT)); }
                catch (IllegalArgumentException exception) { actionMessage("This quest uses an unsupported crop."); return; }
            }
            int remaining = objective.requiredAmount - selected.getObjectiveProgress(objective.id);
            int amount = Math.min(selectedDeliveryAmount, Math.min(remaining, game.getInventory(crop)));
            if (amount < 1) { actionMessage("Not enough " + crop.displayName + " in your harvested inventory."); return; }
            int previousProgress = selected.getObjectiveProgress(objective.id);
            if (!progression.setObjectiveProgress(selected.instanceId, objective.id, previousProgress + amount)) {
                actionMessage("Could not record submission. Please try again.");
                return;
            }
            if (!game.consumeHarvestedCrop(crop, amount)) {
                progression.setObjectiveProgress(selected.instanceId, objective.id, previousProgress);
                actionMessage("Inventory changed. Please try again.");
                return;
            }
            // Complete only after the inventory transaction and objective update both succeed.
            if (previousProgress + amount >= objective.requiredAmount) {
                progression.completeQuest(selected.instanceId);
                PixelGardenProgressionData.QuestInstance updated = progression.getInstance(selected.instanceId);
                if (updated != null && updated.getStatus() == PixelGardenProgressionData.QuestStatus.COMPLETED) {
                    viewStatus = PixelGardenProgressionData.QuestStatus.COMPLETED;
                }
            }
            saveProgressionAsync();
            actionMessage("Submitted " + amount + " " + crop.displayName + ".");
            return;
        }
        if (button.id == CLAIM) {
            PixelGardenProgressionData.QuestInstance selected = selectedQuest();
            PixelGardenProgressionData.QuestDefinition def = definition(selected);
            if (selected == null || def == null || selected.getStatus() != PixelGardenProgressionData.QuestStatus.COMPLETED
                    || selected.areRewardsClaimed() || def.rewards.isEmpty()) {
                actionMessage("No unclaimed rewards are available.");
                return;
            }
            if (!game.grantQuestRewards(def.rewards)) {
                actionMessage("Reward configuration is invalid or exceeds a supported limit.");
                return;
            }
            if (!progression.markRewardsClaimed(selected.instanceId)) {
                actionMessage("Rewards were granted but could not be marked as claimed. Please report this.");
                return;
            }
            saveProgressionAsync();
            actionMessage("Quest rewards claimed.");
            return;
        }
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
        drawCenteredString(fontRendererObj, "QUEST JOURNAL  •  RECORDS OF YOUR GARDEN", width / 2, 23, 0xFFE3E8F0);
        String storylineLine = "CHAPTER " + progression.getStorylineChapter() + ": "
                + progression.getStorylineChapterTitle() + "  |  " + progression.getTutorialObjectiveText();
        storylineLine = fontRendererObj.trimStringToWidth(storylineLine, Math.max(100, width - 24));
        drawCenteredString(fontRendererObj, storylineLine, width / 2, 36, 0xFFFFD77A);

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
        if (!actionMessage.isEmpty()) {
            drawCenteredString(fontRendererObj, actionMessage, width / 2, 51, 0xFF67451F);
        }
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
            if (!def.npc.isEmpty()) { y += 22; drawString(fontRendererObj, "NPC: " + def.npc, detailX + 8, y, 0xFF8A5A20); }
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
                    drawString(fontRendererObj, ("*".equals(objective.target) ? "Any crop" : objective.target) + "  " + progress + "/" + objective.requiredAmount,
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
            if (selected.getStatus() == PixelGardenProgressionData.QuestStatus.ACTIVE) {
                PixelGardenProgressionData.Objective delivery = firstDeliveryObjective(def);
                if (delivery != null) {
                    PixelGardenGame.Crop crop = null;
                    try { crop = PixelGardenGame.Crop.valueOf(delivery.target.toUpperCase(java.util.Locale.ROOT)); }
                    catch (IllegalArgumentException ignored) { }
                    int remaining = delivery.requiredAmount - selected.getObjectiveProgress(delivery.id);
                    int inInventory = crop == null ? 0 : game.getInventory(crop);
                    drawString(fontRendererObj, "Submit amount: " + selectedDeliveryAmount
                            + "  |  Remaining: " + Math.max(0, remaining), detailX + 8, top + h - 39, 0xFF493019);
                    drawString(fontRendererObj, "Harvested in inventory: " + inInventory,
                            detailX + 8, top + h - 28, 0xFF493019);
                }
            }
            String rewardTextStatus = def.rewards.isEmpty() ? "Rewards not configured"
                    : (selected.areRewardsClaimed() ? "Rewards claimed" : "Rewards available on completion");
            drawString(fontRendererObj, statusLabel(selected.getStatus()) + "  |  " + rewardTextStatus,
                    detailX + 8, top + h - 20, selected.getStatus() == PixelGardenProgressionData.QuestStatus.COMPLETED
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
