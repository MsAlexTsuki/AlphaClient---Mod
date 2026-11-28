package com.alpharip.staffmod.aimassistexpansion;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Standalone preview screen for selecting the expansion mode and basic settings. */
public final class AimAssistGui extends GuiScreen {
    private final AimAssistSettings settings;
    private final GuiScreen parent;
    private static final int ENABLE = 1, MODE = 2, PRIORITY = 3, RESET = 4, BACK = 5;

    public AimAssistGui(GuiScreen parent, AimAssistSettings settings) {
        this.parent = parent;
        this.settings = settings;
    }
    @Override
    public void initGui() {
        buttonList.clear();
        int x = width / 2 - 110, y = height / 2 - 55;
        buttonList.add(new GuiButton(ENABLE, x, y, 220, 20, "AIM ASSIST: " + (settings.isEnabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(MODE, x, y + 26, 220, 20, "TYPE: " + settings.getMode().getDisplayName()));
        buttonList.add(new GuiButton(PRIORITY, x, y + 52, 220, 20, "PRIORITY: " + settings.getPriority().name()));
        buttonList.add(new GuiButton(RESET, x, y + 78, 106, 20, "RESET"));
        buttonList.add(new GuiButton(BACK, x + 114, y + 78, 106, 20, "BACK"));
    }
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ENABLE) settings.setEnabled(!settings.isEnabled());
        else if (button.id == MODE) {
            AimAssistMode[] modes = AimAssistMode.values();
            settings.setMode(modes[(settings.getMode().ordinal() + 1) % modes.length]);
        } else if (button.id == PRIORITY) {
            AimTargetSelector.Priority[] values = AimTargetSelector.Priority.values();
            settings.setPriority(values[(settings.getPriority().ordinal() + 1) % values.length]);
        } else if (button.id == RESET) settings.reset();
        else if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        initGui();
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "AIMASSIST EXPANSION", width / 2, height / 2 - 82, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Prototype settings preview - local session only",
                width / 2, height / 2 + 112, 0xFFAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    @Override
    public boolean doesGuiPauseGame() { return false; }
}
