// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Shared AlphaClient button: restrained dark surface, clean border and Alpha Yellow focus. */
public final class AlphaButton extends GuiButton {
    private final boolean primary;
    private final boolean active;

    public AlphaButton(int id, int x, int y, int width, int height, String label) {
        this(id, x, y, width, height, label, false, false);
    }

    public AlphaButton(int id, int x, int y, int width, int height, String label, boolean primary) {
        this(id, x, y, width, height, label, primary, false);
    }

    public AlphaButton(int id, int x, int y, int width, int height, String label,
                       boolean primary, boolean active) {
        super(id, x, y, width, height, UiText.normalize(label));
        this.primary = primary;
        this.active = active;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;

        int background;
        int foreground;
        if (!enabled) {
            background = 0xA512141A;
            foreground = 0xFF777D88;
        } else if (primary) {
            background = hovered ? 0xFF292D36 : 0xFF171A21;
            foreground = 0xFFFFD866;
        } else {
            background = hovered ? 0xE02A2D35 : 0xC014161D;
            foreground = (active || hovered) ? 0xFFFFD866 : 0xFFE6E8ED;
        }

        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, background);
        int border = active ? 0xFFFFD866
                : (primary ? (hovered ? 0xFFFFF0BE : 0xFFFFD866)
                : (hovered ? 0xFFFFD866 : 0x664B515D));
        drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, border);
        drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height,
                primary ? 0x66FFFFFF : 0xAA090A0E);
        drawRect(xPosition, yPosition, xPosition + 1, yPosition + height, border);
        drawRect(xPosition + width - 1, yPosition, xPosition + width, yPosition + height, border);

        drawCenteredString(mc.fontRendererObj, displayString,
                xPosition + width / 2, yPosition + (height - 8) / 2, foreground);
    }
}
