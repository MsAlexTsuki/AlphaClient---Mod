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
 * Compact HUD presentation for the most recent kill groups and their drops.
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
        if (entries.isEmpty()) {
            return;
        }

        FontRenderer font = minecraft.fontRendererObj;
        ScaledResolution resolution = new ScaledResolution(minecraft);
        int x = resolution.getScaledWidth() - PANEL_WIDTH - 8;
        int y = 8;
        int lineHeight = font.FONT_HEIGHT + 3;
        int height = PADDING * 2 + lineHeight * 2;

        for (LootFeedEntry entry : entries) {
            height += lineHeight;
            height += entry.getDrops().size() * lineHeight;
            height += 3;
        }

        Gui.drawRect(x, y, x + PANEL_WIDTH, y + height, 0xB9000000);
        Gui.drawRect(x, y, x + PANEL_WIDTH, y + 1, 0xFFFFC83D);

        int textX = x + PADDING;
        int textY = y + PADDING;

        font.drawString("LOOT FEED", textX, textY, 0xFFFFC83D);
        textY += lineHeight + 1;

        for (LootFeedEntry entry : entries) {
            String heading = entry.getKillCount() + "x " + entry.getEntityName();
            font.drawString(trimToWidth(font, heading, PANEL_WIDTH - PADDING * 2),
                    textX, textY, 0xFFFFFFFF);
            textY += lineHeight;

            for (Map.Entry<String, Integer> drop : entry.getDrops().entrySet()) {
                String line = "- " + drop.getKey() + ": " + drop.getValue();
                font.drawString(trimToWidth(font, line, PANEL_WIDTH - PADDING * 2),
                        textX + 5, textY, 0xFFCCCCCC);
                textY += lineHeight;
            }
            textY += 3;
        }
    }

    private String trimToWidth(FontRenderer font, String text, int width) {
        if (font.getStringWidth(text) <= width) {
            return text;
        }
        return font.trimStringToWidth(text, width - font.getStringWidth("...")) + "...";
    }
}
