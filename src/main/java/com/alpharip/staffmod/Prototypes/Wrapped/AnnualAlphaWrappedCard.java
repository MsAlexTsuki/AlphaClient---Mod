package com.alpharip.staffmod.wrapped;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

/** Small reusable statistic card renderer for the Minecraft 1.8.9 Wrapped screen. */
public final class AnnualAlphaWrappedCard {
    private AnnualAlphaWrappedCard() { }

    public static void draw(Minecraft mc, int left, int top, int width, int height,
                            String label, String value, int accentColor) {
        if (mc == null || mc.fontRendererObj == null) return;
        Gui.drawRect(left, top, left + width, top + height, 0xF014171E);
        Gui.drawRect(left, top, left + width, top + 2, accentColor);
        mc.fontRendererObj.drawString(label == null ? "" : label, left + 9, top + 9, 0xFF9DA5B3);
        String safeValue = value == null ? "—" : value;
        mc.fontRendererObj.drawString(safeValue, left + 9, top + 25, 0xFFF2F3F5);
    }
}
