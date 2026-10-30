// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.misc.CloudHeightSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** AlphaClient controls for Minecraft's existing cloud renderer. */
public final class CloudHeightGui extends AlphaGuiScreen {
    private static final int CLOUDS = 1, STYLE = 2, LOWER = 3, RAISE = 4, BACK = 5;
    private final GuiScreen parent;

    public CloudHeightGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 55;
        buttonList.add(new AlphaButton(CLOUDS, center - 100, top, 200, 20, cloudsLabel(),
                CloudHeightSettings.isCloudsEnabled()));
        buttonList.add(new AlphaButton(STYLE, center - 100, top + 27, 200, 20, styleLabel()));
        buttonList.add(new AlphaButton(LOWER, center - 100, top + 54, 40, 20, "-"));
        buttonList.add(new AlphaButton(RAISE, center + 60, top + 54, 40, 20, "+"));
        buttonList.add(new AlphaButton(BACK, center - 100, top + 91, 200, 20, "BACK"));
    }

    private static String cloudsLabel() {
        return "Clouds: " + (CloudHeightSettings.isCloudsEnabled() ? "ON" : "OFF");
    }

    private static String styleLabel() {
        return "Cloud Style: " + (CloudHeightSettings.getCloudStyle() == 1 ? "FAST" : "FANCY");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == CLOUDS) CloudHeightSettings.toggleClouds();
        else if (button.id == STYLE) CloudHeightSettings.cycleCloudStyle();
        else if (button.id == LOWER) CloudHeightSettings.changeCloudHeight(-4.0F);
        else if (button.id == RAISE) CloudHeightSettings.changeCloudHeight(4.0F);
        else if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawCenteredString(fontRendererObj, "CLOUD HEIGHT", width / 2, height / 2 - 91, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Cloud Height: " + (int) CloudHeightSettings.getCloudHeight(),
                width / 2, height / 2 + 5, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Use +/- to adjust the Y level", width / 2,
                height / 2 + 25, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
