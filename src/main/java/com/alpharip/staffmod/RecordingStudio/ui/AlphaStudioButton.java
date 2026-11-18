package com.alpharip.staffmod.RecordingStudio.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

/** Compact dark-and-gold button used by the standalone Recording Studio screens. */
public final class AlphaStudioButton extends GuiButton {
    private final boolean accent;

    public AlphaStudioButton(int id, int x, int y, int width, int height, String label) {
        this(id, x, y, width, height, label, false);
    }

    public AlphaStudioButton(int id, int x, int y, int width, int height, String label, boolean accent) {
        super(id, x, y, width, height, label);
        this.accent = accent;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        int background = !enabled ? 0xFF242424
                : accent ? (hovered ? 0xFFFFD65A : 0xFFC88F20)
                : (hovered ? 0xFF343434 : 0xFF252525);
        int border = accent ? 0xFFFFD65A : (hovered ? 0xFF8D762F : 0xFF454545);
        Gui.drawRect(xPosition, yPosition, xPosition + width, yPosition + height, border);
        Gui.drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, background);
        int textColor = !enabled ? 0xFF777777 : accent ? 0xFF17130A : 0xFFE6E6E6;
        drawCenteredString(minecraft.fontRendererObj, displayString,
                xPosition + width / 2, yPosition + (height - 8) / 2, textColor);
    }
}
