package com.alpharip.staffmod.ArrowCounter;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.Locale;

/** Standalone settings screen for the ArrowCounter HUD prototype. */
public final class ArrowCounterGui extends GuiScreen {
    private final GuiScreen parent;
    private final ArrowCounterSettings settings;
    private final ArrowCounter module;

    public ArrowCounterGui(GuiScreen parent, ArrowCounterSettings settings, ArrowCounter module) {
        this.parent = parent;
        this.settings = settings;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 72;

        buttonList.add(new GuiButton(0, center - 100, top, 200, 20, enabledText()));
        buttonList.add(new GuiButton(1, center - 100, top + 26, 98, 20, "Scale -"));
        buttonList.add(new GuiButton(2, center + 2, top + 26, 98, 20, "Scale +"));
        buttonList.add(new GuiButton(3, center - 100, top + 52, 98, 20, "X - 5"));
        buttonList.add(new GuiButton(4, center + 2, top + 52, 98, 20, "X + 5"));
        buttonList.add(new GuiButton(5, center - 100, top + 78, 98, 20, "Y - 5"));
        buttonList.add(new GuiButton(6, center + 2, top + 78, 98, 20, "Y + 5"));
        buttonList.add(new GuiButton(7, center - 100, top + 112, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: settings.setEnabled(!settings.isEnabled()); break;
            case 1: settings.adjustScale(-0.1F); break;
            case 2: settings.adjustScale(0.1F); break;
            case 3: settings.adjustX(-5); break;
            case 4: settings.adjustX(5); break;
            case 5: settings.adjustY(-5); break;
            case 6: settings.adjustY(5); break;
            case 7:
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
        int center = width / 2;
        int top = height / 2 - 94;
        drawCenteredString(fontRendererObj, "ArrowCounter", center, top, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "Position: " + settings.getX() + ", " + settings.getY()
                        + "   Scale: " + String.format(Locale.ROOT, "%.1f", settings.getScale()),
                center, top + 132, 0xAAAAAA);
        drawCenteredString(fontRendererObj, "Counts arrows in your player inventory",
                center, top + 146, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String enabledText() {
        return "Enabled: " + (settings.isEnabled() ? "ON" : "OFF");
    }
}
