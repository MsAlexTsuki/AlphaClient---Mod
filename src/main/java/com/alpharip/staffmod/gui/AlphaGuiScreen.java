package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.FontRenderer;

/** Base screen that applies AlphaClient's display capitalization consistently. */
public class AlphaGuiScreen extends GuiScreen {
    protected static void drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        GuiScreen.drawString(fontRenderer, UiText.normalize(text), x, y, color);
    }

    protected static void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        GuiScreen.drawCenteredString(fontRenderer, UiText.normalize(text), x, y, color);
    }
}
