package com.alpharip.staffmod.screenshotcomparison;

import net.minecraft.client.gui.GuiScreen;

/**
 * Entry point for the standalone screenshot-comparison prototype.
 * The prototype is intentionally not registered in the client yet.
 */
public final class ScreenshotComparison {
    private ScreenshotComparison() {
    }

    public static void open(GuiScreen parent) {
        net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(
                new ScreenshotComparisonScreen(parent));
    }
}
