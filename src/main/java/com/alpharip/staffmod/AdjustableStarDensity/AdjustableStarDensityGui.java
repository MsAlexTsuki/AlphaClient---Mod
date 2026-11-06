package com.alpharip.staffmod.AdjustableStarDensity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Standalone Minecraft 1.8.9 settings preview; not registered in an existing menu. */
public final class AdjustableStarDensityGui extends GuiScreen {
    private static final int ENABLE = 1;
    private static final int STYLE = 2;
    private static final int RESET = 3;
    private static final int DONE = 4;

    private final GuiScreen parent;
    private final StarDensitySettings settings;
    private boolean draggingDensity;

    public AdjustableStarDensityGui(GuiScreen parent, StarDensitySettings settings) {
        this.parent = parent;
        if (settings == null) throw new IllegalArgumentException("settings cannot be null");
        this.settings = settings;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int x = width / 2 - 100;
        buttonList.add(new GuiButton(ENABLE, x, height / 2 + 49, 200, 20, enabledLabel()));
        buttonList.add(new GuiButton(STYLE, x, height / 2 + 73, 200, 20, styleLabel()));
        buttonList.add(new GuiButton(RESET, x - 104, height / 2 + 101, 100, 20, "Reset"));
        buttonList.add(new GuiButton(DONE, x + 4, height / 2 + 101, 96, 20, "Done"));
    }

    private String enabledLabel() {
        return "Star Density: " + (settings.isEnabled() ? "ON" : "OFF");
    }

    private String styleLabel() {
        return "Style: " + settings.getStyle().getDisplayName();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == ENABLE) settings.setEnabled(!settings.isEnabled());
        else if (button.id == STYLE) settings.setStyle(settings.getStyle().next());
        else if (button.id == RESET) settings.reset();
        else if (button.id == DONE) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = width / 2 - 170;
        int top = height / 2 - 105;
        int right = width / 2 + 170;
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawRect(left, top + 2, right, height / 2 + 40, 0xE6101218);
        drawCenteredString(fontRendererObj, "ADJUSTABLE STAR DENSITY", width / 2, top + 15, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Density multiplier for the night-sky star field", width / 2, top + 34, 0xFFB8BEC8);
        drawDensitySlider(mouseX, mouseY);
        drawCenteredString(fontRendererObj,
                settings.getDensityPercent() + "%  (" + settings.getEffectiveDensityMultiplier() + "x effective)",
                width / 2, height / 2 + 13, 0xFFFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDensitySlider(int mouseX, int mouseY) {
        int left = width / 2 - 135;
        int right = width / 2 + 135;
        int y = height / 2 - 22;
        drawString(fontRendererObj, "Star density", left, y - 17, 0xFFFFFFFF);
        drawRect(left, y, right, y + 4, 0xFF454A54);
        int knob = left + Math.round((right - left) * settings.getDensityPercent()
                / (float) StarDensitySettings.MAX_DENSITY);
        drawRect(left, y, knob, y + 4, 0xFFFFD866);
        drawRect(knob - 4, y - 4, knob + 5, y + 8, 0xFFFFD866);
        drawString(fontRendererObj, "0%", left, y + 12, 0xFFB8BEC8);
        String max = "200%";
        drawString(fontRendererObj, max, right - fontRendererObj.getStringWidth(max), y + 12, 0xFFB8BEC8);
    }

    private void updateDensity(int mouseX) {
        int left = width / 2 - 135;
        int right = width / 2 + 135;
        float progress = Math.max(0.0F, Math.min(1.0F, (mouseX - left) / (float) (right - left)));
        settings.setDensityPercent(Math.round(progress * StarDensitySettings.MAX_DENSITY));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && mouseX >= width / 2 - 140 && mouseX <= width / 2 + 140
                && mouseY >= height / 2 - 29 && mouseY <= height / 2 - 10) {
            draggingDensity = true;
            updateDensity(mouseX);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
        if (button == 0 && draggingDensity) updateDensity(mouseX);
        super.mouseClickMove(mouseX, mouseY, button, elapsed);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggingDensity = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
