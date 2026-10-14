package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Compact shared AlphaClient button styling with a restrained Alpha Yellow accent. */
public final class AlphaButton extends GuiButton {
    private final boolean primary;

    public AlphaButton(int id, int x, int y, int width, int height, String label) {
        this(id, x, y, width, height, label, false);
    }

    public AlphaButton(int id, int x, int y, int width, int height, String label, boolean primary) {
        super(id, x, y, width, height, label);
        this.primary = primary;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;

        int background = !enabled ? 0xFF191B20
                : primary ? (hovered ? 0xFFFFE49A : 0xFFFFD866)
                : (hovered ? 0xFF2B2E36 : 0xFF202229);
        int foreground = !enabled ? 0xFF666B75
                : primary ? 0xFF17191E : (hovered ? 0xFFFFD866 : 0xFFE9EAF0);

        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + 1,
                primary ? 0x55FFFFFF : (hovered ? 0xFFFFD866 : 0xFF393D47));
        drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height, 0xFF14161B);
        if (!primary) {
            drawRect(xPosition, yPosition, xPosition + 2, yPosition + height,
                    hovered ? 0xFFFFD866 : 0xFF393D47);
        } else {
            drawRect(xPosition, yPosition + height - 2, xPosition + width, yPosition + height, 0x66FFFFFF);
        }

        drawCenteredString(mc.fontRendererObj, displayString,
                xPosition + width / 2, yPosition + (height - 8) / 2, foreground);
    }
}
