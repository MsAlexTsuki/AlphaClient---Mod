package com.alpharip.staffmod.LootFeed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Map;

/**
 * Compact HUD presentation for the oldest-to-newest kill groups and their drops.
 */
public final class LootFeedHud {
    private static final int PANEL_WIDTH = 170;
    private static final int PADDING = 6;
    private final LootFeedTracker tracker;

    public LootFeedHud(LootFeedTracker tracker) {
        this.tracker = tracker;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || minecraft.theWorld == null
                || minecraft.gameSettings.hideGUI) {
            return;
        }

        List<LootFeedEntry> entries = tracker.getVisibleEntries();
        List<LootFeedTracker.FadingEntry> fadingEntries = tracker.getFadingEntries();
        if (entries.isEmpty() && fadingEntries.isEmpty()) {
            return;
        }

        FontRenderer font = minecraft.fontRendererObj;
        ScaledResolution resolution = new ScaledResolution(minecraft);
        int x = resolution.getScaledWidth() - PANEL_WIDTH - 8;
        int y = 8;
        int lineHeight = font.FONT_HEIGHT + 3;
        int height = PADDING * 2 + lineHeight * 2;

        for (LootFeedTracker.FadingEntry fading : fadingEntries) {
            height += getEntryHeight(fading.getEntry(), lineHeight);
        }
        for (LootFeedEntry entry : entries) {
            height += getEntryHeight(entry, lineHeight);
        }

        Gui.drawRect(x, y, x + PANEL_WIDTH, y + height, 0xB9000000);
        Gui.drawRect(x, y, x + PANEL_WIDTH, y + 1, 0xFFFFC83D);

        int textX = x + PADDING;
        int textY = y + PADDING;

        font.drawString("LOOT FEED", textX, textY, 0xFFFFC83D);
        textY += lineHeight + 1;

        // Evicted groups fade independently above the active groups.
        for (LootFeedTracker.FadingEntry fading : fadingEntries) {
            textY = drawEntry(font, fading.getEntry(), textX, textY,
                    lineHeight, fading.getAlpha());
        }

        // Active groups remain oldest at the top and newest at the bottom.
        for (LootFeedEntry entry : entries) {
            textY = drawEntry(font, entry, textX, textY, lineHeight, 255);
        }
    }

    private int getEntryHeight(LootFeedEntry entry, int lineHeight) {
        if (entry.getDrops().isEmpty()) {
            return 0;
        }
        return lineHeight + entry.getDrops().size() * lineHeight + 3;
    }

    private int drawEntry(FontRenderer font, LootFeedEntry entry, int textX,
                          int textY, int lineHeight, int alpha) {
        if (entry.getDrops().isEmpty() || alpha <= 0) {
            return textY;
        }

        String heading = entry.getKillCount() + "x " + entry.getEntityName();
        font.drawString(trimToWidth(font, heading, PANEL_WIDTH - PADDING * 2),
                textX, textY, withAlpha(0xFFFFFF, alpha));
        textY += lineHeight;

        for (Map.Entry<String, Integer> drop : entry.getDrops().entrySet()) {
            String line = "- " + drop.getKey() + ": " + drop.getValue();
            font.drawString(trimToWidth(font, line, PANEL_WIDTH - PADDING * 2),
                    textX + 5, textY, withAlpha(0xCCCCCC, alpha));
            textY += lineHeight;
        }
        return textY + 3;
    }

    private int withAlpha(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    private String trimToWidth(FontRenderer font, String text, int width) {
        if (font.getStringWidth(text) <= width) {
            return text;
        }
        return font.trimStringToWidth(text, width - font.getStringWidth("...")) + "...";
    }
}
