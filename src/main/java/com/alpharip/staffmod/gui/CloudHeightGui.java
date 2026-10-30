// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.misc.CloudHeightSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** AlphaClient controls for Minecraft's existing cloud renderer. */
public final class CloudHeightGui extends AlphaGuiScreen {
    private static final int CLOUDS = 1, STYLE = 2, BACK = 5;
    private static final int SLIDER_WIDTH = 200;
    private final GuiScreen parent;
    private int sliderX, sliderY;

    public CloudHeightGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 55;
        sliderX = center - SLIDER_WIDTH / 2;
        sliderY = top + 64;
        buttonList.add(new AlphaButton(CLOUDS, center - 100, top, 200, 20, cloudsLabel(),
                CloudHeightSettings.isCloudsEnabled()));
        buttonList.add(new AlphaButton(STYLE, center - 100, top + 27, 200, 20, styleLabel()));
        buttonList.add(new AlphaButton(BACK, center - 100, top + 105, 200, 20, "BACK"));
    }

    private static String cloudsLabel() {
        return "Clouds: " + (CloudHeightSettings.isCloudsEnabled() ? "ON" : "OFF");
    }

    private static String styleLabel() {
        return "Cloud Style: " + (CloudHeightSettings.getCloudStyle() == 1 ? "FAST" : "FANCY");
    }

    private void updateSlider(int mouseX) {
        float fraction = Math.max(0.0F, Math.min(1.0F, (mouseX - sliderX) / (float) SLIDER_WIDTH));
        float target = Math.round(fraction * 256.0F);
        float difference = target - CloudHeightSettings.getCloudHeight();
        if (Math.abs(difference) >= 0.5F) CloudHeightSettings.changeCloudHeight(difference);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && mouseY >= sliderY - 5 && mouseY <= sliderY + 9
                && mouseX >= sliderX - 2 && mouseX <= sliderX + SLIDER_WIDTH + 2) {
            updateSlider(mouseX);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceClick);
        if (clickedMouseButton == 0) updateSlider(mouseX);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == CLOUDS) CloudHeightSettings.toggleClouds();
        else if (button.id == STYLE) CloudHeightSettings.cycleCloudStyle();
        else if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawCenteredString(fontRendererObj, "CLOUD HEIGHT", width / 2, height / 2 - 91, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Cloud Height: " + (int) CloudHeightSettings.getCloudHeight(),
                width / 2, sliderY - 17, 0xFFFFD866);
        drawRect(sliderX, sliderY, sliderX + SLIDER_WIDTH, sliderY + 4, 0xFF555B66);
        int knobX = sliderX + Math.round((CloudHeightSettings.getCloudHeight() / 256.0F) * SLIDER_WIDTH);
        drawRect(knobX - 3, sliderY - 4, knobX + 4, sliderY + 8, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Y level: 0 - 256", width / 2, sliderY + 14, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
