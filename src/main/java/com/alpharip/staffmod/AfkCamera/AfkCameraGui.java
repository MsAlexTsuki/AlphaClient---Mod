package com.alpharip.staffmod.AfkCamera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Standalone Minecraft 1.8.9 settings preview; not registered in an existing menu. */
public final class AfkCameraGui extends GuiScreen {
    private static final int ENABLE = 1;
    private static final int DELAY = 2;
    private static final int STYLE = 3;
    private static final int RESET = 4;
    private static final int DONE = 5;

    private final GuiScreen parent;
    private final AfkCameraSettings settings;

    public AfkCameraGui(GuiScreen parent, AfkCameraSettings settings) {
        this.parent = parent;
        if (settings == null) throw new IllegalArgumentException("settings cannot be null");
        this.settings = settings;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int x = width / 2 - 105;
        int y = height / 2 - 5;
        buttonList.add(new GuiButton(ENABLE, x, y, 210, 20,
                "AFK Camera: " + (settings.isEnabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(DELAY, x, y + 25, 210, 20,
                "AFK Delay: " + settings.getIdleDelaySeconds() + " seconds"));
        buttonList.add(new GuiButton(STYLE, x, y + 50, 210, 20,
                "Camera Style: " + settings.getStyle().getDisplayName()));
        buttonList.add(new GuiButton(RESET, x - 2, y + 80, 102, 20, "Reset"));
        buttonList.add(new GuiButton(DONE, x + 108, y + 80, 102, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == ENABLE) {
            settings.setEnabled(!settings.isEnabled());
        } else if (button.id == DELAY) {
            int next = settings.getIdleDelaySeconds() + 15;
            if (next > AfkCameraSettings.MAX_IDLE_DELAY_SECONDS) {
                next = AfkCameraSettings.MIN_IDLE_DELAY_SECONDS;
            }
            settings.setIdleDelaySeconds(next);
        } else if (button.id == STYLE) {
            settings.setStyle(settings.getStyle().next());
        } else if (button.id == RESET) {
            settings.reset();
        } else if (button.id == DONE) {
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
        drawRect(left, top + 2, right, height / 2 - 16, 0xE6101218);
        drawCenteredString(fontRendererObj, "AFK CAMERA", width / 2, top + 15, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "Select behavior for periods of inactivity",
                width / 2, top + 35, 0xFFB8BEC8);
        drawCenteredString(fontRendererObj, "Camera movement and AFK input hooks are not connected",
                width / 2, height / 2 - 28, 0xFFFFAA66);
        super.drawScreen(mouseX, mouseY, partialTicks);
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
