// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Folder-style feature card using the same visual language as General module tiles. */
public final class FeatureTileButton extends GuiButton {
    private static final int YELLOW = 0xFFFFD866;
    private final String name;
    private final String icon;

    public FeatureTileButton(int id, int x, int y, int width, int height,
                             String name, String icon, boolean enabled) {
        super(id, x, y, width, height, "");
        this.name = name;
        this.icon = icon;
        this.enabled = enabled;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        int bg = !enabled ? 0xA512141A : (hovered ? 0xF0272B34 : 0xE0181B22);
        int edge = !enabled ? 0x664B515D : (hovered ? YELLOW : 0x664B515D);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, bg);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, edge);
        drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height, 0xAA090A0E);
        drawRect(xPosition, yPosition, xPosition + 1, yPosition + height, edge);
        drawRect(xPosition + width - 1, yPosition, xPosition + width, yPosition + height, edge);

        int iconWidth = mc.fontRendererObj.getStringWidth(icon);
        mc.fontRendererObj.drawString(icon, xPosition + (width - iconWidth) / 2,
                yPosition + 21, enabled ? 0xFFE5E7EB : 0xFF777D88, false);
        String label = name;
        int maxWidth = width - 10;
        if (mc.fontRendererObj.getStringWidth(label) > maxWidth) {
            while (label.length() > 3 && mc.fontRendererObj.getStringWidth(label + "...") > maxWidth) {
                label = label.substring(0, label.length() - 1);
            }
            label += "...";
        }
        mc.fontRendererObj.drawString(UiText.normalize(label),
                xPosition + (width - mc.fontRendererObj.getStringWidth(label)) / 2,
                yPosition + height - 11, enabled ? 0xFFF0F1F4 : 0xFF777D88, false);
    }
}
