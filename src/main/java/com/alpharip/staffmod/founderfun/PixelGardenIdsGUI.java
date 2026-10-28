package com.alpharip.staffmod.founderfun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

/** Scrollable registry of the quest IDs and NPC references currently present in the Pixel Garden catalog. */
public final class PixelGardenIdsGUI extends GuiScreen {
    private final GuiScreen parent;
    private final PixelGardenProgressionData progression = PixelGardenGame.getInstance().getProgressionData();
    private final List<String> entries = new ArrayList<String>();
    private int scrollOffset;

    public PixelGardenIdsGUI(GuiScreen parent) {
        this.parent = parent;
        progression.ensureDefaultCatalog();
        try { progression.mergeSharedDefinitions(SupabaseCommunityService.getSharedPixelGardenQuests()); }
        catch (Exception exception) { System.err.println("[AlphaClient] Could not sync quest IDs: " + exception.getMessage()); }
        rebuildEntries();
    }

    private void rebuildEntries() {
        entries.clear();
        Set<String> npcs = new LinkedHashSet<String>();
        for (PixelGardenProgressionData.QuestDefinition definition : progression.getDefinitions()) {
            entries.add("QUEST ID  |  " + definition.id + "  —  " + definition.name);
            if (definition.npc != null && !definition.npc.trim().isEmpty()) {
                npcs.add(definition.npc.trim());
            }
        }
        entries.add("");
        entries.add("REGISTERED NPC IDS");
        for (java.util.Map.Entry<String, String> npc : PixelGardenNPC.getAllNpcNames().entrySet()) {
            entries.add("NPC  |  " + npc.getValue() + "  |  " + npc.getKey());
            npcs.add(npc.getKey());
        }
        entries.add("");
        entries.add("NPC REFERENCES USED BY QUESTS");
        if (npcs.isEmpty()) {
            entries.add("No NPC references are configured in the current quest catalog.");
        } else {
            for (String npc : npcs) {
                entries.add("NPC  |  " + PixelGardenNPC.displayName(npc) + "  |  " + npc);
            }
        }
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 55, height - 30, 110, 20, "Close"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == 0) mc.displayGuiScreen(parent);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int visible = Math.max(1, (height - 100) / 14);
            scrollOffset = Math.max(0, Math.min(Math.max(0, entries.size() - visible),
                    scrollOffset + (wheel < 0 ? 3 : -3)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, 44, 0xF00B1018);
        drawRect(0, 42, width, 45, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "PIXEL GARDEN ID REGISTRY", width / 2, 8, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "Quest IDs and NPC references from the current catalog", width / 2, 25, 0xFFE3E8F0);
        int visible = Math.max(1, (height - 100) / 14);
        int end = Math.min(entries.size(), scrollOffset + visible);
        int y = 56;
        for (int i = scrollOffset; i < end; i++) {
            String entry = entries.get(i);
            if (entry.isEmpty()) {
                y += 5;
                continue;
            }
            int color = (entry.equals("REGISTERED NPC IDS") || entry.equals("NPC REFERENCES USED BY QUESTS"))
                    ? 0xFFFFD45A
                    : entry.startsWith("QUEST ID") ? 0xFFE7D5A6
                    : entry.startsWith("NPC  |") ? 0xFFB7C4D5 : 0xFF9CA7B7;
            drawString(fontRendererObj, fontRendererObj.trimStringToWidth(entry, width - 32), 16, y, color);
            y += 14;
        }
        drawCenteredString(fontRendererObj, "Scroll to browse  " + (entries.isEmpty() ? 0 : scrollOffset + 1)
                + "-" + Math.min(entries.size(), scrollOffset + visible) + "/" + entries.size(),
                width / 2, height - 46, 0xFFB7C4D5);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
