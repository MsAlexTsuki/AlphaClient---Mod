// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Dedicated scrollable Quest ID List, styled consistently with the Pixel Garden Command Guide. */
public final class PixelGardenQuestIdListGUI extends GuiScreen {
    private static final int BACK = 1;
    private final GuiScreen parent;
    private final PixelGardenProgressionData progression = PixelGardenGame.getInstance().getProgressionData();
    private final List<String> lines = new ArrayList<String>();
    private int scrollOffset;

    public PixelGardenQuestIdListGUI(GuiScreen parent) {
        this.parent = parent;
        progression.ensureDefaultCatalog();
        try {
            progression.mergeSharedDefinitions(SupabaseCommunityService.getSharedPixelGardenQuests());
        } catch (Exception exception) {
            System.err.println("[AlphaClient] Could not sync Pixel Garden Quest ID List: " + exception.getMessage());
        }
        rebuildLines();
    }

    private void rebuildLines() {
        lines.clear();
        lines.add("QUEST IDS");
        List<PixelGardenProgressionData.QuestDefinition> definitions = progression.getDefinitions();
        if (definitions.isEmpty()) {
            lines.add("No quest definitions are available in the current catalog.");
        } else {
            for (PixelGardenProgressionData.QuestDefinition definition : definitions) {
                lines.add("QUEST ID: " + definition.id);
                lines.add("Name: " + definition.name);
                lines.add("NPC ID: " + (definition.npc == null || definition.npc.trim().isEmpty()
                        ? "Not assigned" : definition.npc.trim()));
                lines.add("Rewards: " + (definition.rewards == null || definition.rewards.isEmpty()
                        ? "None configured" : definition.rewards.toString()));
                lines.add("");
            }
        }

        lines.add("REGISTERED NPC IDS");
        for (java.util.Map.Entry<String, String> npc : PixelGardenNPC.getAllNpcNames().entrySet()) {
            lines.add(npc.getValue() + " — " + npc.getKey());
        }
        lines.add("");
        lines.add("FOUNDER COMMANDS");
        lines.add("/pixelgarden createquest admin <quest id> <description> <NPC ID> <amount> <reward type>");
        lines.add("/pixelgarden rewards add <quest id> <type> <amount>");
        lines.add("/pixelgarden rewards remove <quest id> <type> <amount|all>");
        lines.add("");
        lines.add("Use the exact QUEST ID shown above when configuring rewards.");
        lines.add("Use a registered NPC ID when creating a quest.");
        lines.add("The list shows the current local and shared quest catalog.");
        lines.add("Use the mouse wheel to scroll through the Quest ID List.");
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new AlphaButton(BACK, width / 2 - 70, height - 32, 140, 22, "Back to Garden", true));
        int max = Math.max(0, lines.size() - visibleLineCount());
        scrollOffset = Math.max(0, Math.min(scrollOffset, max));
    }

    private int visibleLineCount() {
        return Math.max(5, (height - 105) / 13);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int max = Math.max(0, lines.size() - visibleLineCount());
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel < 0 ? 3 : -3)));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK && mc != null) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, 46, 0xF00B1018);
        drawRect(0, 44, width, 47, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "PIXEL GARDEN | QUEST ID LIST", width / 2, 15, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "Quest identifiers, NPC assignments and configured rewards",
                width / 2, 31, 0xFFB7C4D5);

        int left = Math.max(18, width / 2 - 220);
        int right = Math.min(width - 18, width / 2 + 220);
        int top = 54;
        int bottom = height - 43;
        drawRect(left, top, right, bottom, 0xE510141C);
        drawRect(left, top, right, top + 1, 0xFFFFC83D);

        int end = Math.min(lines.size(), scrollOffset + visibleLineCount());
        int y = top + 9;
        for (int i = scrollOffset; i < end; i++) {
            String line = lines.get(i);
            if (line.isEmpty()) {
                y += 5;
                continue;
            }
            boolean heading = "QUEST IDS".equals(line) || "REGISTERED NPC IDS".equals(line)
                    || "FOUNDER COMMANDS".equals(line);
            int color = heading ? 0xFFFFC83D
                    : (line.startsWith("QUEST ID:") ? 0xFFE7D5A6
                    : line.startsWith("/pixelgarden") ? 0xFFE5EAF2 : 0xFFB7C4D5);
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(line, right - left - 20),
                    left + 10, y, color);
            y += 13;
        }
        if (lines.size() > visibleLineCount()) {
            drawCenteredString(fontRendererObj, "Scroll " + (scrollOffset + 1) + "-" + end + " / " + lines.size(),
                    width / 2, height - 44, 0xFF9DA8B8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
