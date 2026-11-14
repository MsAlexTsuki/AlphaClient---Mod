// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Compact feature card styled to match General's ModuleTileButton. */
public final class FeatureTileButton extends GuiButton {
    private static final int YELLOW = 0xFFFFD866;
    private final String name;
    private final String icon;
    private final String status;
    private final boolean active;

    public FeatureTileButton(int id, int x, int y, int width, int height,
                             String name, String icon, String status,
                             boolean active, boolean clickable) {
        super(id, x, y, width, height, "");
        this.name = name;
        this.icon = icon;
        this.status = status;
        this.active = active;
        this.enabled = clickable;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;

        int bg = hovered ? 0xF0272B34 : 0xE0181B22;
        int edge = hovered ? YELLOW : 0x664B515D;
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, bg);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, edge);
        drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height, 0xAA090A0E);
        drawRect(xPosition, yPosition, xPosition + 1, yPosition + height, edge);
        drawRect(xPosition + width - 1, yPosition, xPosition + width, yPosition + height, edge);

        if (status != null) {
            int stateColor = "ON".equals(status) ? YELLOW
                    : ("PLANNED".equals(status) ? 0xFF8A8F99 : 0xFFB8BDC8);
            mc.fontRendererObj.drawString(UiText.normalize(status),
                    xPosition + 7, yPosition + 4, stateColor, false);
        }

        int iconWidth = mc.fontRendererObj.getStringWidth(icon);
        int iconY = yPosition + Math.max(10, (height - 8) / 2 - 1);
        mc.fontRendererObj.drawString(icon, xPosition + (width - iconWidth) / 2,
                iconY, 0xFFE5E7EB, false);

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
                yPosition + height - 10, 0xFFF0F1F4, false);
    }
}
