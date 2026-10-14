package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Alpha yellow/charcoal button used by the new client screens. */
public final class AlphaButton extends GuiButton {
    public AlphaButton(int id, int x, int y, int width, int height, String label) {
        super(id, x, y, width, height, label);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        int background = !enabled ? 0xFF25272D : hovered ? 0xFF3B3527 : 0xFF202229;
        int outline = hovered && enabled ? 0xFFFFD866 : 0xFF454954;
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, outline);
        drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height, outline);
        drawRect(xPosition, yPosition, xPosition + 1, yPosition + height, outline);
        drawRect(xPosition + width - 1, yPosition, xPosition + width, yPosition + height, outline);
        int textColor = !enabled ? 0xFF777B84 : hovered ? 0xFFFFD866 : 0xFFE8E9ED;
        drawCenteredString(mc.fontRendererObj, displayString,
                xPosition + width / 2, yPosition + (height - 8) / 2, textColor);
    }
}