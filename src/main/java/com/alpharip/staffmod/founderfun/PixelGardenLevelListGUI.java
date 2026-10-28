// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Scrollable Pixel Garden level and reward reference. */
public final class PixelGardenLevelListGUI extends GuiScreen {
    private static final int BACK = 1;
    private final GuiScreen parent;
    private final List<String> lines = new ArrayList<String>();
    private int scrollOffset;

    public PixelGardenLevelListGUI(GuiScreen parent) {
        this.parent = parent;
        rebuildLines();
    }

    private void rebuildLines() {
        lines.clear();
        PixelGardenProgressionData data = PixelGardenGame.getInstance().getProgressionData();
        Map<Integer, Long> xp = data.getLevelXpRequirements();
        Map<Integer, String> rewards = data.getLevelRewards();
        Map<Integer, String> descriptions = data.getLevelDescriptions();
        lines.add("PIXEL GARDEN LEVELS & REWARDS");
        lines.add("XP shown is the amount needed to advance from that level.");
        lines.add("");
        int maxLevel = Math.max(100, data.getLevel());
        for (Integer configuredLevel : xp.keySet()) if (configuredLevel != null) maxLevel = Math.max(maxLevel, configuredLevel.intValue());
        for (Integer configuredLevel : rewards.keySet()) if (configuredLevel != null) maxLevel = Math.max(maxLevel, configuredLevel.intValue());
        for (int level = 1; level <= maxLevel; level++) {
            lines.add("LEVEL " + level + "  |  Next level: " + xp.get(level) + " XP");
            String desc = descriptions.get(level);
            if (desc != null && !desc.trim().isEmpty()) lines.add("  " + desc);
            String reward = rewards.get(level);
            lines.add("  Rewards: " + (reward == null || reward.trim().isEmpty() ? "None configured" : reward));
            lines.add("");
        }
        lines.add("Founder commands: /pixelgarden editlevel rewards <level> <rewards>");
        lines.add("/pixelgarden editlevel xp <level> <xp>");
        lines.add("/pixelgarden editlevel desc <level> <desc>");
    }

    @Override public void initGui() {
        buttonList.clear();
        buttonList.add(new AlphaButton(BACK, width / 2 - 70, height - 32, 140, 22, "Back to Garden", true));
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, lines.size() - visibleLineCount())));
    }

    private int visibleLineCount() { return Math.max(5, (height - 100) / 13); }

    @Override public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) scrollOffset = Math.max(0, Math.min(Math.max(0, lines.size() - visibleLineCount()),
                scrollOffset + (wheel < 0 ? 3 : -3)));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == BACK) mc.displayGuiScreen(parent);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "PIXEL GARDEN LEVELS", width / 2, 18, 0xFFFFC83D);
        int y = 42;
        int max = Math.min(lines.size(), scrollOffset + visibleLineCount());
        for (int i = scrollOffset; i < max; i++) {
            String line = lines.get(i);
            int color = line.startsWith("LEVEL ") || line.startsWith("PIXEL GARDEN") ? 0xFFFFC83D : 0xFFE2E6ED;
            drawString(fontRendererObj, line, Math.max(18, width / 2 - 210), y, color);
            y += 13;
        }
        drawCenteredString(fontRendererObj, "Scroll to view all levels and rewards", width / 2, height - 48, 0xFF9DA7B5);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
