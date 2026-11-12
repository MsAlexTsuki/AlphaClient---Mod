package com.alpharip.staffmod.AirHud;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Standalone settings screen; it is not connected to an existing menu. */
public final class AirHudGui extends GuiScreen {
    private final GuiScreen parent;
    private final AirHudSettings settings;
    private final AirHud module;

    public AirHudGui(GuiScreen parent, AirHudSettings settings, AirHud module) {
        this.parent = parent;
        this.settings = settings;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        buttonList.add(new GuiButton(0, center - 100, height / 2 - 48, 200, 20, enabledText()));
        buttonList.add(new GuiButton(1, center - 100, height / 2 - 22, 200, 20, labelText()));
        buttonList.add(new GuiButton(2, center - 100, height / 2 + 4, 200, 20, positionText()));
        buttonList.add(new GuiButton(3, center - 100, height / 2 + 30, 200, 20, "Color: Cycle"));
        buttonList.add(new GuiButton(4, center - 100, height / 2 + 62, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: settings.setEnabled(!settings.isEnabled()); break;
            case 1: settings.setShowLabel(!settings.isShowLabel()); break;
            case 2: settings.cyclePosition(); break;
            case 3: settings.setColorIndex((settings.getColorIndex() + 1) % 5); break;
            case 4:
                module.save();
                mc.displayGuiScreen(parent);
                return;
            default: return;
        }
        initGui();
    }

    @Override
    public void onGuiClosed() {
        module.save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Air HUD", width / 2, height / 2 - 82, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Displays remaining air while underwater",
                width / 2, height / 2 + 92, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String enabledText() { return "Enabled: " + (settings.isEnabled() ? "ON" : "OFF"); }
    private String labelText() { return "Show label: " + (settings.isShowLabel() ? "ON" : "OFF"); }
    private String positionText() {
        return "Position: " + settings.getPosition().name().replace('_', ' ');
    }
}
