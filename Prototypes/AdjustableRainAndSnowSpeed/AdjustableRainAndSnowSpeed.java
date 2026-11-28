package com.alpharip.staffmod.AdjustableRainAndSnowSpeed;

import net.minecraft.client.gui.GuiScreen;

/**
 * Main facade for independently adjustable rain and snow animation speeds.
 * This is an isolated prototype: it does not alter Minecraft's weather renderer.
 */
public final class AdjustableRainAndSnowSpeed {
    public boolean enabled = true;
    public String style = "default";

    private final WeatherSpeedSettings settings = new WeatherSpeedSettings();
    private final WeatherSpeedController controller = new WeatherSpeedController(settings);

    public WeatherSpeedSettings getSettings() { return settings; }
    public WeatherSpeedController getController() { return controller; }

    public GuiScreen createSettingsGui(GuiScreen parent) {
        return new AdjustableRainAndSnowSpeedGui(parent, settings);
    }
}
