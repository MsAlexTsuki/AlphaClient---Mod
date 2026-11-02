package com.alpharip.staffmod.Minimap;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiScreen;

/** Large view of the live AlphaClient minimap. */
public final class FullscreenMinimapGui extends AlphaGuiScreen {
    private final GuiScreen parent;

    public FullscreenMinimapGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xE6080A0F);
        drawRect(0, 0, width, 2, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "ALPHA WORLD MAP", width / 2, 12, 0xFFFFD866);
        AlphaMinimap.getInstance().drawFullscreenMap(width, height);
        drawCenteredString(fontRendererObj, "Press ESC to close", width / 2, height - 18, 0xFFB8BEC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
