// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** In-game dialogue and quest interaction screen for the registered Pixel Garden NPCs. */
public final class PixelGardenNPCGUI extends GuiScreen {
    private static final int NPC_BASE = 100;
    private static final int ACTION = 200;
    private static final int QUEST_BOOK = 201;
    private static final int BACK = 202;

    private final GuiScreen parent;
    private final PixelGardenGame game = PixelGardenGame.getInstance();
    private final PixelGardenProgressionData progression = game.getProgressionData();
    private String selectedNpc = PixelGardenNPC.TUTORIAL;
    private String message = "";

    public PixelGardenNPCGUI(GuiScreen parent) {
        this.parent = parent;
        progression.ensureDefaultCatalog();
        try {
            progression.mergeSharedDefinitions(SupabaseCommunityService.getSharedPixelGardenQuests());
        } catch (Exception exception) {
            System.err.println("[AlphaClient] Could not sync Pixel Garden NPC quests: " + exception.getMessage());
        }
    }

    @Override
    public void initGui() {
        buttonList.clear();
        List<String> ids = new ArrayList<String>(PixelGardenNPC.getAllNpcNames().keySet());
        int left = Math.max(12, width / 2 - 210);
        int top = 72;
        int buttonWidth = 126;
        int buttonHeight = 20;
        int gap = 6;
        for (int i = 0; i < ids.size(); i++) {
            int col = i % 3;
            int row = i / 3;
            String id = ids.get(i);
            buttonList.add(new AlphaButton(NPC_BASE + i, left + col * (buttonWidth + gap),
                    top + row * (buttonHeight + gap), buttonWidth, buttonHeight,
                    (id.equals(selectedNpc) ? "> " : "") + PixelGardenNPC.displayName(id)));
        }

        int actionY = height - 62;
        if (PixelGardenNPC.TUTORIAL.equals(selectedNpc)) {
            buttonList.add(new AlphaButton(ACTION, width / 2 - 150, actionY, 150, 22,
                    progression.isTutorialComplete() ? "Review Tutorial" : "Start / Continue Tutorial"));
        } else {
            PixelGardenProgressionData.QuestInstance available = firstQuestForNpc(
                    selectedNpc, PixelGardenProgressionData.QuestStatus.AVAILABLE);
            if (available != null) {
                buttonList.add(new AlphaButton(ACTION, width / 2 - 150, actionY, 150, 22, "Accept Quest"));
            }
        }
        buttonList.add(new AlphaButton(QUEST_BOOK, width / 2 + 6, actionY, 144, 22, "Open Quest Book"));
        buttonList.add(new AlphaButton(BACK, width / 2 - 62, height - 32, 124, 20, "Back"));
    }

    private PixelGardenProgressionData.QuestInstance firstQuestForNpc(
            String npcId, PixelGardenProgressionData.QuestStatus status) {
        for (PixelGardenProgressionData.QuestInstance instance : progression.getInstances()) {
            PixelGardenProgressionData.QuestDefinition definition = progression.getDefinition(instance.definitionId);
            if (definition != null && npcId.equals(definition.npc) && instance.getStatus() == status) return instance;
        }
        return null;
    }

    private int countQuestsForNpc(String npcId) {
        int count = 0;
        for (PixelGardenProgressionData.QuestInstance instance : progression.getInstances()) {
            PixelGardenProgressionData.QuestDefinition definition = progression.getDefinition(instance.definitionId);
            if (definition != null && npcId.equals(definition.npc)
                    && instance.getStatus() != PixelGardenProgressionData.QuestStatus.ABANDONED) count++;
        }
        return count;
    }

    private void saveProgressionAsync() {
        if (!SupabaseCommunityService.isSignedIn()) return;
        final com.google.gson.JsonObject snapshot = game.toSaveData();
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    if (SupabaseCommunityService.isSignedIn()) SupabaseCommunityService.savePixelGardenSave(snapshot);
                } catch (Exception exception) {
                    System.err.println("[AlphaClient] Pixel Garden NPC save failed: " + exception.getMessage());
                }
            }
        }, "AlphaClient-PixelGardenNPCSave");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id >= NPC_BASE && button.id < NPC_BASE + PixelGardenNPC.getAllNpcNames().size()) {
            List<String> ids = new ArrayList<String>(PixelGardenNPC.getAllNpcNames().keySet());
            selectedNpc = ids.get(button.id - NPC_BASE);
            message = "";
            initGui();
            return;
        }
        if (button.id == BACK) {
            saveProgressionAsync();
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == QUEST_BOOK) {
            saveProgressionAsync();
            mc.displayGuiScreen(new PixelGardenQuestBookGUI(this));
            return;
        }
        if (button.id == ACTION && PixelGardenNPC.TUTORIAL.equals(selectedNpc)) {
            progression.ensureDefaultCatalog();
            PixelGardenProgressionData.QuestInstance tutorial = null;
            for (PixelGardenProgressionData.QuestInstance instance : progression.getInstances()) {
                if ("pg_fresh_start".equals(instance.definitionId)
                        && instance.getStatus() != PixelGardenProgressionData.QuestStatus.ABANDONED) {
                    tutorial = instance;
                    break;
                }
            }
            if (tutorial == null) {
                String instanceId = progression.assignQuest("pg_fresh_start");
                tutorial = instanceId == null ? null : progression.getInstance(instanceId);
            }
            if (tutorial != null && tutorial.getStatus() == PixelGardenProgressionData.QuestStatus.AVAILABLE) {
                boolean accepted = progression.acceptQuest(tutorial.instanceId, progression.getLevel(), currentPrestige());
                message = accepted ? "Vincent: Your tutorial has started. Check your Quest Book."
                        : "Vincent: You cannot accept this quest right now. Check your active quest limit and requirements.";
            } else if (progression.isTutorialComplete()) {
                message = "Vincent: You have completed the tutorial. Keep growing your garden!";
            } else {
                message = "Vincent: Welcome back. Continue with your current tutorial objective.";
            }
            saveProgressionAsync();
            initGui();
            return;
        }
        if (button.id == ACTION) {
            PixelGardenProgressionData.QuestInstance available = firstQuestForNpc(
                    selectedNpc, PixelGardenProgressionData.QuestStatus.AVAILABLE);
            if (available == null) {
                message = PixelGardenNPC.displayName(selectedNpc) + ": I have no available quest for you right now.";
            } else {
                boolean accepted = progression.acceptQuest(available.instanceId, progression.getLevel(), currentPrestige());
                message = accepted ? PixelGardenNPC.displayName(selectedNpc) + ": Quest accepted."
                        : "You cannot accept this quest right now. Check your active quest limit and requirements.";
            }
            saveProgressionAsync();
            initGui();
        }
    }

    private int currentPrestige() {
        if (!SupabaseCommunityService.isSignedIn()) return 0;
        try {
            com.google.gson.JsonObject state = SupabaseCommunityService.getPixelGardenPrestigeState();
            return state.has("prestige_count") ? Math.max(0, state.get("prestige_count").getAsInt()) : 0;
        } catch (Exception ignored) { return 0; }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(width - 24, 460);
        int left = (width - panelWidth) / 2;
        drawRect(left, 28, left + panelWidth, height - 20, 0xEDE9D9B8);
        drawRect(left, 28, left + panelWidth, 30, 0xFFFFC83D);
        drawRect(left, 30, left + 2, height - 20, 0xFF8E6539);
        drawCenteredString(fontRendererObj, "PIXEL GARDEN  •  NPCs", width / 2, 38, 0xFF493019);
        drawCenteredString(fontRendererObj, "Choose a character to talk and view their quests.",
                width / 2, 53, 0xFF705A3D);

        int infoY = 174;
        String name = PixelGardenNPC.displayName(selectedNpc);
        drawCenteredString(fontRendererObj, name, width / 2, infoY, 0xFF8A5A20);
        drawCenteredString(fontRendererObj, selectedNpc, width / 2, infoY + 13, 0xFF705A3D);
        if (PixelGardenNPC.TUTORIAL.equals(selectedNpc)) {
            drawCenteredString(fontRendererObj, "I'm Vincent, your guide to Pixel Garden.", width / 2,
                    infoY + 32, 0xFF493019);
            drawCenteredString(fontRendererObj, progression.isTutorialComplete()
                    ? "Tutorial complete — you can review your progress."
                    : "Current objective: " + progression.getTutorialObjectiveText(),
                    width / 2, infoY + 45, 0xFF493019);
        } else {
            int count = countQuestsForNpc(selectedNpc);
            drawCenteredString(fontRendererObj, count == 0 ? "No quests are assigned to this NPC yet."
                    : "Quests linked to this NPC: " + count, width / 2, infoY + 34, 0xFF493019);
            PixelGardenProgressionData.QuestInstance active = firstQuestForNpc(
                    selectedNpc, PixelGardenProgressionData.QuestStatus.ACTIVE);
            PixelGardenProgressionData.QuestInstance available = firstQuestForNpc(
                    selectedNpc, PixelGardenProgressionData.QuestStatus.AVAILABLE);
            PixelGardenProgressionData.QuestInstance shown = active != null ? active : available;
            if (shown != null) {
                PixelGardenProgressionData.QuestDefinition def = progression.getDefinition(shown.definitionId);
                if (def != null) {
                    drawCenteredString(fontRendererObj, def.name + "  •  " + shown.getStatus().name(),
                            width / 2, infoY + 48, 0xFF493019);
                    drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth(def.description, panelWidth - 30),
                            width / 2, infoY + 61, 0xFF705A3D);
                }
            }
        }
        if (!message.isEmpty()) drawCenteredString(fontRendererObj,
                fontRendererObj.trimStringToWidth(message, panelWidth - 24), width / 2, height - 82, 0xFF67451F);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
