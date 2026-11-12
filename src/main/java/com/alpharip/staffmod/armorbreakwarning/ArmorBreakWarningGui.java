package com.alpharip.staffmod.armorbreakwarning;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Standalone settings screen; it is not connected to an existing client menu. */
public final class ArmorBreakWarningGui extends GuiScreen {
    private final GuiScreen parent;
    private final ArmorBreakWarningSettings settings;
    private final ArmorBreakWarning module;

    public ArmorBreakWarningGui(GuiScreen parent, ArmorBreakWarningSettings settings,
                                 ArmorBreakWarning module) {
        this.parent = parent;
        this.settings = settings;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        buttonList.add(new GuiButton(0, center - 100, height / 2 - 24, 200, 20, enabledText()));
        buttonList.add(new GuiButton(1, center - 100, height / 2 + 4, 200, 20, thresholdText()));
        buttonList.add(new GuiButton(2, center - 100, height / 2 + 38, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                settings.setEnabled(!settings.isEnabled());
                break;
            case 1:
                settings.cycleThreshold();
                break;
            case 2:
                module.save();
                mc.displayGuiScreen(parent);
                return;
            default:
                return;
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
        drawCenteredString(fontRendererObj, "Armor Break Warning", width / 2, height / 2 - 62, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Warns when equipped armor is close to breaking",
                width / 2, height / 2 + 76, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String enabledText() {
        return "Enabled: " + (settings.isEnabled() ? "ON" : "OFF");
    }

    private String thresholdText() {
        return "Warning threshold: " + settings.getThresholdPercent() + "%";
    }
}
