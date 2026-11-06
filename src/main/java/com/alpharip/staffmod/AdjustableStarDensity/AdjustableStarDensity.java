package com.alpharip.staffmod.AdjustableStarDensity;

import net.minecraft.client.gui.GuiScreen;

/**
 * Main facade for the Adjustable Star Density prototype.
 * The module exposes local settings and a standalone preview screen only.
 */
public final class AdjustableStarDensity {
    public boolean enabled = true;
    public String style = "default";

    private final StarDensitySettings settings = new StarDensitySettings();
    private final StarDensityController controller = new StarDensityController(settings);

    public StarDensitySettings getSettings() { return settings; }
    public StarDensityController getController() { return controller; }

    public GuiScreen createSettingsGui(GuiScreen parent) {
        return new AdjustableStarDensityGui(parent, settings);
    }
}
