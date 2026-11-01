// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/** Read-only seven-day Alpha World Edit audit log viewer. */
public final class WorldEditAuditGui extends GuiScreen {
    private final GuiScreen parent;
    private List<WorldEditAuditLog.AuditEntry> entries;
    private int scrollOffset;
    private int selected = -1;
    private int listTop, listBottom, listLeft, listRight;

    public WorldEditAuditGui(GuiScreen parent) {
        this.parent = parent;
        refresh();
    }

    private void refresh() {
        entries = WorldEditAuditLog.getEntries();
        if (selected >= entries.size()) selected = entries.size() - 1;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(1, 8, height - 28, 70, 20, "Back"));
        buttonList.add(new GuiButton(2, width - 82, height - 28, 74, 20, "Refresh"));
        listTop = 54;
        listBottom = height - 38;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == 1) mc.displayGuiScreen(parent);
        else if (button.id == 2) refresh();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset = Math.max(0, Math.min(Math.max(0, entries.size() - 1),
                    scrollOffset + (wheel > 0 ? -2 : 2)));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 || mouseX < listLeft || mouseX > listRight
                || mouseY < listTop || mouseY >= listBottom) return;
        int index = scrollOffset + (mouseY - listTop) / 30;
        if (index >= 0 && index < entries.size()) selected = index;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = Math.min(720, width - 24);
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        listLeft = left + 10;
        listRight = right - 10;
        listTop = 58;
        listBottom = height - 38;

        drawRect(left, 12, right, listBottom + 4, 0xE00A0C11);
        drawRect(left, 12, right, 14, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ALPHA WORLD EDIT — AUDIT LOG", width / 2, 20, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Local history · last 7 days · newest first",
                width / 2, 36, 0xFFB8B8B8);
        drawRect(listLeft, listTop, listRight, listBottom, 0xAA111318);

        if (entries.isEmpty()) {
            drawCenteredString(fontRendererObj, "No Alpha World Edit changes recorded in the last 7 days.",
                    width / 2, listTop + 18, 0xFFBDBDBD);
        } else {
            int visible = Math.max(1, (listBottom - listTop) / 30);
            for (int row = 0; row < visible; row++) {
                int index = scrollOffset + row;
                if (index >= entries.size()) break;
                WorldEditAuditLog.AuditEntry entry = entries.get(index);
                int y = listTop + row * 30;
                drawRect(listLeft + 2, y + 1, listRight - 2, y + 28,
                        index == selected ? 0x885A4720 : 0x33222222);
                drawString(fontRendererObj, trim(entry.action, panelWidth - 180),
                        listLeft + 8, y + 4, 0xFFFFFFFF);
                drawString(fontRendererObj, format(entry.timestamp) + "  ·  "
                        + entry.changed + " block(s) changed", listLeft + 8, y + 16, 0xFFBDBDBD);
            }
        }

        int detailX = right - 270;
        if (selected >= 0 && selected < entries.size()) {
            WorldEditAuditLog.AuditEntry entry = entries.get(selected);
            drawRect(detailX, listTop + 2, right - 12, listBottom - 2, 0xDD17191F);
            drawString(fontRendererObj, "CHANGE DETAILS", detailX + 8, listTop + 10, 0xFFFFD866);
            drawString(fontRendererObj, trim(entry.action, 245), detailX + 8, listTop + 26, 0xFFFFFFFF);
            drawString(fontRendererObj, format(entry.timestamp), detailX + 8, listTop + 40, 0xFFBDBDBD);
            drawString(fontRendererObj, "Dimension: " + entry.dimension, detailX + 8, listTop + 54, 0xFFBDBDBD);
            drawString(fontRendererObj, "Changed: " + entry.changed, detailX + 8, listTop + 68, 0xFFBDBDBD);
            drawString(fontRendererObj, "Bounds: " + entry.minX + "," + entry.minY + "," + entry.minZ,
                    detailX + 8, listTop + 82, 0xFFBDBDBD);
            drawString(fontRendererObj, "to " + entry.maxX + "," + entry.maxY + "," + entry.maxZ,
                    detailX + 8, listTop + 94, 0xFFBDBDBD);
            int y = listTop + 114;
            drawString(fontRendererObj, "BLOCK CHANGES", detailX + 8, y, 0xFFFFD866);
            y += 13;
            for (String change : entry.changes) {
                if (y > listBottom - 75) break;
                drawString(fontRendererObj, trim(change, 245), detailX + 8, y, 0xFFE0E0E0);
                y += 12;
            }
            y += 5;
            drawString(fontRendererObj, "SAMPLE COORDINATES", detailX + 8, y, 0xFFFFD866);
            y += 13;
            for (String sample : entry.samples) {
                if (y > listBottom - 8) break;
                drawString(fontRendererObj, trim(sample, 245), detailX + 8, y, 0xFFBDBDBD);
                y += 11;
            }
        } else {
            drawString(fontRendererObj, "Select an entry to inspect block changes.", detailX + 8,
                    listTop + 12, 0xFFBDBDBD);
        }

        drawString(fontRendererObj, "Click an entry for details · Scroll to browse · ESC to close",
                88, height - 22, 0xFFAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String format(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return format.format(new Date(timestamp));
    }

    private String trim(String value, int maxPixels) {
        String result = value == null ? "" : value;
        while (!result.isEmpty() && fontRendererObj.getStringWidth(result) > maxPixels)
            result = result.substring(0, result.length() - 1);
        return result;
    }
}
