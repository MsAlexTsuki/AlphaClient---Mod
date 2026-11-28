package com.alpharip.staffmod.ChestItemTotal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChest;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

/** Draws the aggregated chest contents in a compact panel. */
final class ChestItemTotalRenderer {
    private static final int MAX_VISIBLE_ROWS = 10;

    private final ChestItemTotalSettings settings;
    private final ChestItemTotalScanner scanner;

    ChestItemTotalRenderer(ChestItemTotalSettings settings,
                           ChestItemTotalScanner scanner) {
        this.settings = settings;
        this.scanner = scanner;
    }

    void render(GuiScreen screen) {
        if (!settings.isEnabled() || !(screen instanceof GuiChest)) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null) return;

        ChestItemTotalSnapshot snapshot = scanner.getSnapshot();
        List<ChestItemTotalEntry> entries = snapshot.getEntries();
        int visibleRows = Math.min(entries.size(), MAX_VISIBLE_ROWS);
        int extraRows = entries.size() - visibleRows;
        int panelWidth = 174;
        int panelHeight = 31 + visibleRows * 11 + (extraRows > 0 ? 11 : 0);
        int left = settings.getX();
        int top = settings.getY();

        Gui.drawRect(left, top, left + panelWidth, top + panelHeight, 0xB0101010);
        Gui.drawRect(left, top, left + panelWidth, top + 1, 0xFFFFC83D);
        mc.fontRendererObj.drawStringWithShadow("Chest Item Total", left + 6, top + 5, 0xFFFFC83D);
        mc.fontRendererObj.drawStringWithShadow(
                "Total: " + snapshot.getTotalItems() + "  |  Types: "
                        + snapshot.getUniqueItemTypes(),
                left + 6, top + 17, 0xFFFFFFFF);

        for (int i = 0; i < visibleRows; i++) {
            ChestItemTotalEntry entry = entries.get(i);
            int rowY = top + 31 + i * 11;
            String name = mc.fontRendererObj.trimStringToWidth(
                    entry.getDisplayName(), 112);
            mc.fontRendererObj.drawStringWithShadow(name, left + 6, rowY, 0xFFE0E0E0);
            String count = Integer.toString(entry.getCount());
            mc.fontRendererObj.drawStringWithShadow(count,
                    left + panelWidth - 6 - mc.fontRendererObj.getStringWidth(count),
                    rowY, 0xFFFFFFFF);
        }

        if (extraRows > 0) {
            mc.fontRendererObj.drawStringWithShadow(
                    "+ " + extraRows + " more item types",
                    left + 6, top + 31 + visibleRows * 11, 0xFFAAAAAA);
        }
    }
}
