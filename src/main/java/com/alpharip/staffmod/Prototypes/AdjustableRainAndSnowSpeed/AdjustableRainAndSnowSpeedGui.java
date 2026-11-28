package com.alpharip.staffmod.AdjustableRainAndSnowSpeed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.Locale;

/** Standalone Minecraft 1.8.9 settings preview; not registered in an existing menu. */
public final class AdjustableRainAndSnowSpeedGui extends GuiScreen {
    private static final int ENABLE = 1;
    private static final int RAIN_TOGGLE = 2;
    private static final int SNOW_TOGGLE = 3;
    private static final int RESET = 4;
    private static final int DONE = 5;
    private final GuiScreen parent;
    private final WeatherSpeedSettings settings;
    private int dragging;

    public AdjustableRainAndSnowSpeedGui(GuiScreen parent, WeatherSpeedSettings settings) {
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int x = width / 2 - 100;
        buttonList.add(new GuiButton(ENABLE, x, height / 2 + 57, 200, 20, enabledLabel()));
        buttonList.add(new GuiButton(RAIN_TOGGLE, x - 108, height / 2 + 82, 104, 20, rainLabel()));
        buttonList.add(new GuiButton(SNOW_TOGGLE, x + 104, height / 2 + 82, 104, 20, snowLabel()));
        buttonList.add(new GuiButton(RESET, x - 108, height / 2 + 108, 104, 20, "Reset"));
        buttonList.add(new GuiButton(DONE, x + 4, height / 2 + 108, 96, 20, "Done"));
    }

    private String enabledLabel() { return "Weather Speed: " + (settings.isEnabled() ? "ON" : "OFF"); }
    private String rainLabel() { return "Rain: " + (settings.isRainEnabled() ? "ON" : "OFF"); }
    private String snowLabel() { return "Snow: " + (settings.isSnowEnabled() ? "ON" : "OFF"); }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == ENABLE) settings.setEnabled(!settings.isEnabled());
        else if (button.id == RAIN_TOGGLE) settings.setRainEnabled(!settings.isRainEnabled());
        else if (button.id == SNOW_TOGGLE) settings.setSnowEnabled(!settings.isSnowEnabled());
        else if (button.id == RESET) settings.reset();
        else if (button.id == DONE) Minecraft.getMinecraft().displayGuiScreen(parent);
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = width / 2 - 170;
        int top = height / 2 - 112;
        int right = width / 2 + 170;
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawRect(left, top + 2, right, height / 2 + 48, 0xE6101218);
        drawCenteredString(fontRendererObj, "ADJUSTABLE RAIN AND SNOW SPEED", width / 2, top + 15, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Independent animation speed multipliers", width / 2, top + 33, 0xFFB8BEC8);
        drawSlider("Rain speed", settings.getRainMultiplier(), height / 2 - 42, mouseX, mouseY);
        drawSlider("Snow speed", settings.getSnowMultiplier(), height / 2 + 5, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSlider(String label, float value, int y, int mouseX, int mouseY) {
        int left = width / 2 - 135;
        int right = width / 2 + 135;
        drawString(fontRendererObj, label, left, y - 14, 0xFFFFFFFF);
        String valueText = String.format(Locale.ROOT, "%.2fx", value);
        drawString(fontRendererObj, valueText, right - fontRendererObj.getStringWidth(valueText), y - 14, 0xFFFFD866);
        int knob = left + Math.round((right - left) * (value - WeatherSpeedSettings.MIN_MULTIPLIER)
                / (WeatherSpeedSettings.MAX_MULTIPLIER - WeatherSpeedSettings.MIN_MULTIPLIER));
        drawRect(left, y, right, y + 4, 0xFF454A54);
        drawRect(left, y, knob, y + 4, 0xFFFFD866);
        drawRect(knob - 4, y - 4, knob + 5, y + 8, 0xFFFFD866);
    }

    private void updateSlider(int mouseX, int id) {
        int left = width / 2 - 135;
        int right = width / 2 + 135;
        float progress = Math.max(0.0F, Math.min(1.0F, (mouseX - left) / (float)(right - left)));
        float value = WeatherSpeedSettings.MIN_MULTIPLIER
                + progress * (WeatherSpeedSettings.MAX_MULTIPLIER - WeatherSpeedSettings.MIN_MULTIPLIER);
        if (id == 1) settings.setRainMultiplier(value);
        else if (id == 2) settings.setSnowMultiplier(value);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int left = width / 2 - 140;
            int right = width / 2 + 140;
            if (mouseX >= left && mouseX <= right && mouseY >= height / 2 - 49 && mouseY <= height / 2 - 30) dragging = 1;
            else if (mouseX >= left && mouseX <= right && mouseY >= height / 2 - 2 && mouseY <= height / 2 + 17) dragging = 2;
            if (dragging != 0) { updateSlider(mouseX, dragging); return; }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
        if (button == 0 && dragging != 0) updateSlider(mouseX, dragging);
        super.mouseClickMove(mouseX, mouseY, button, elapsed);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = 0;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { Minecraft.getMinecraft().displayGuiScreen(parent); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
