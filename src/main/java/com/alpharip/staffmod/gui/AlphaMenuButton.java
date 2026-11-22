// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Rounded button renderer scoped exclusively to AlphaModMenu. */
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
            background = 0xA512151D;
            foreground = 0xFF777F8D;
        } else if (primary) {
            background = hovered ? 0xFF34302A : 0xFF25231F;
            foreground = 0xFFFFD866;
        } else {
            background = hovered ? 0xE02A303B : 0xC0181D27;
            foreground = (active || hovered) ? 0xFFFFD866 : 0xFFE6E8ED;
        }

        if (!transparentSurface) {
            AlphaMenuRender.roundedRect(xPosition, yPosition, xPosition + width, yPosition + height,
                    Math.min(6, height / 2.0F), background);
        } else if (hovered) {
            AlphaMenuRender.roundedRect(xPosition, yPosition, xPosition + width, yPosition + height,
                    Math.min(6, height / 2.0F), 0x332A303B);
        }
        int border = active || primary ? 0xAAFFD866 : (hovered ? 0x99FFD866 : 0x334B5563);
        AlphaMenuRender.roundedOutline(xPosition, yPosition, xPosition + width, yPosition + height,
                Math.min(6, height / 2.0F), border);
        if (!transparentSurface) {
            drawCenteredString(mc.fontRendererObj, displayString, xPosition + width / 2,
                    yPosition + (height - 8) / 2, foreground);
        }
    }
}
