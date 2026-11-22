// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Shared AlphaClient button: restrained dark surface, clean border and Alpha Yellow focus. */
public final class AlphaMenuButton extends GuiButton {
    private final boolean primary;
    private final boolean active;
    private final boolean transparentSurface;

    public AlphaMenuButton(int id, int x, int y, int width, int height, String label) {
        this(id, x, y, width, height, label, false, false, false);
    }

    public AlphaMenuButton(int id, int x, int y, int width, int height, String label, boolean primary) {
        this(id, x, y, width, height, label, primary, false, false);
    }

    public AlphaMenuButton(int id, int x, int y, int width, int height, String label,
                       boolean primary, boolean active) {
        this(id, x, y, width, height, label, primary, active, false);
    }

    public AlphaMenuButton(int id, int x, int y, int width, int height, String label,
                       boolean primary, boolean active, boolean transparentSurface) {
        super(id, x, y, width, height, UiText.normalize(label));
        this.primary = primary;
        this.active = active;
        this.transparentSurface = transparentSurface;
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

        int border = active ? 0xFFFFD866
                : (primary ? (hovered ? 0xFFFFF0BE : 0xFFFFD866)
                : (hovered ? 0xFFFFD866 : 0x664B515D));
        if (!transparentSurface) {
            AlphaGuiDraw.roundedRect(xPosition, yPosition, width, height, 5F, border);
            AlphaGuiDraw.roundedRect(xPosition + 1, yPosition + 1, width - 2, height - 2,
                    4F, background);
        } else if (hovered) {
            AlphaGuiDraw.roundedRect(xPosition, yPosition, width, height, 5F, 0x332A2D35);
        }

        if (!transparentSurface) {
            drawCenteredString(mc.fontRendererObj, displayString,
                    xPosition + width / 2, yPosition + (height - 8) / 2, foreground);
        }
    }
}
