package com.alpharip.staffmod.timing.brewing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Small standalone settings screen for the Brewing Timer prototype. */
final class BrewingTimerSettingsScreen extends GuiScreen {
    private final GuiScreen parent;
    private final BrewingTimer timer;

    BrewingTimerSettingsScreen(GuiScreen parent, BrewingTimer timer) {
        this.parent = parent;
        this.timer = timer;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        buttonList.add(new GuiButton(0, center - 100, height / 2 - 24, 200, 20,
                "Module: " + (timer.isEnabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(1, center - 100, height / 2 + 2, 200, 20,
                "HUD: " + (timer.isHudEnabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(2, center - 100, height / 2 + 34, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            timer.setEnabled(!timer.isEnabled());
            button.displayString = "Module: " + (timer.isEnabled() ? "ON" : "OFF");
        } else if (button.id == 1) {
            timer.setHudEnabled(!timer.isHudEnabled());
            button.displayString = "HUD: " + (timer.isHudEnabled() ? "ON" : "OFF");
        } else if (button.id == 2) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Brewing Timer", width / 2, height / 2 - 66, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "Shows the remaining vanilla brewing time.", width / 2, height / 2 - 48, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
