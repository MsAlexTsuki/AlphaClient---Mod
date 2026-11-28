package com.alpharip.staffmod.ChestItemTotal;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Small settings screen for the ChestItemTotal summary panel. */
public final class ChestItemTotalGui extends GuiScreen {
    private final GuiScreen parent;
    private final ChestItemTotalSettings settings;
    private final ChestItemTotal module;

    public ChestItemTotalGui(GuiScreen parent, ChestItemTotalSettings settings,
                             ChestItemTotal module) {
        this.parent = parent;
        this.settings = settings;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 48;
        buttonList.add(new GuiButton(0, center - 100, top, 200, 20,
                "Enabled: " + (settings.isEnabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(1, center - 100, top + 26, 98, 20, "X - 5"));
        buttonList.add(new GuiButton(2, center + 2, top + 26, 98, 20, "X + 5"));
        buttonList.add(new GuiButton(3, center - 100, top + 52, 98, 20, "Y - 5"));
        buttonList.add(new GuiButton(4, center + 2, top + 52, 98, 20, "Y + 5"));
        buttonList.add(new GuiButton(5, center - 100, top + 82, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: settings.setEnabled(!settings.isEnabled()); break;
            case 1: settings.adjustX(-5); break;
            case 2: settings.adjustX(5); break;
            case 3: settings.adjustY(-5); break;
            case 4: settings.adjustY(5); break;
            case 5:
                module.save();
                mc.displayGuiScreen(parent);
                return;
            default: return;
        }
        module.save();
        initGui();
    }

    @Override
    public void onGuiClosed() {
        module.save();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "ChestItemTotal", width / 2,
                height / 2 - 70, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "Panel position: " + settings.getX() + ", " + settings.getY(),
                width / 2, height / 2 + 52, 0xFFAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
