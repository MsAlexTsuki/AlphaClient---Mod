package com.alpharip.staffmod.BuildTimelapse.ui;

import com.alpharip.staffmod.BuildTimelapse.capture.CaptureSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public final class CaptureSettingsScreen extends GuiScreen {
    private final GuiScreen parent;
    private final CaptureSettings settings;
    private String status = "Changes apply to the next captured frame";

    public CaptureSettingsScreen(GuiScreen parent, CaptureSettings settings) {
        if (parent == null || settings == null) throw new IllegalArgumentException();
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int x = width / 2 - 100;
        int y = height / 2 - 60;
        buttonList.add(new GuiButton(0, x, y, 200, 20,
                "Interval: " + settings.getInterval() + " ms"));
        buttonList.add(new GuiButton(1, x, y + 24, 200, 20,
                "HUD hidden: " + settings.isHideHud()));

        GuiButton handButton = new GuiButton(2, x, y + 48, 200, 20,
                "Hand hidden: Unsupported");
        handButton.enabled = false;
        buttonList.add(handButton);

        GuiButton resolutionButton = new GuiButton(3, x, y + 72, 200, 20,
                "Resolution: Native only");
        resolutionButton.enabled = false;
        buttonList.add(resolutionButton);

        buttonList.add(new GuiButton(4, x, y + 104, 200, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            long[] values = { 250L, 500L, 1000L, 2000L, 5000L, 10000L };
            int next = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i] == settings.getInterval()) next = (i + 1) % values.length;
            }
            settings.setInterval(values[next]);
        } else if (button.id == 1) {
            settings.setHideHud(!settings.isHideHud());
        } else if (button.id == 4) {
            mc.displayGuiScreen(parent);
            return;
        }
        initGui();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "CAPTURE SETTINGS", width / 2, 28, 0xFFC83D);
        drawCenteredString(fontRendererObj, status, width / 2, height - 24, 0xBBBBBB);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
