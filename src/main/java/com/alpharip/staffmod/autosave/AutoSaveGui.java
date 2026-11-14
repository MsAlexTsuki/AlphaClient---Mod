package com.alpharip.staffmod.autosave;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.Locale;

/** Settings screen for the AutoSave HUD prototype. */
public final class AutoSaveGui extends GuiScreen {
    private final GuiScreen parent;
    private final AutoSave module;
    private final AutoSaveSettings settings;

    public AutoSaveGui(GuiScreen parent, AutoSave module) {
        this.parent = parent;
        this.module = module;
        this.settings = module.getSettings();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 92;
        buttonList.add(new GuiButton(0, center - 100, top, 200, 20, enabledLabel()));
        buttonList.add(new GuiButton(1, center - 100, top + 25, 200, 20, hudLabel()));
        buttonList.add(new GuiButton(2, center - 100, top + 50, 98, 20, "Window - 1s"));
        buttonList.add(new GuiButton(3, center + 2, top + 50, 98, 20, "Window + 1s"));
        buttonList.add(new GuiButton(4, center - 100, top + 76, 98, 20, "Scale -"));
        buttonList.add(new GuiButton(5, center + 2, top + 76, 98, 20, "Scale +"));
        buttonList.add(new GuiButton(6, center - 100, top + 102, 98, 20, "X - 5"));
        buttonList.add(new GuiButton(7, center + 2, top + 102, 98, 20, "X + 5"));
        buttonList.add(new GuiButton(8, center - 100, top + 128, 98, 20, "Y - 5"));
        buttonList.add(new GuiButton(9, center + 2, top + 128, 98, 20, "Y + 5"));
        buttonList.add(new GuiButton(10, center - 100, top + 156, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: settings.setEnabled(!settings.isEnabled()); break;
            case 1: settings.setShowSaveState(!settings.isShowSaveState()); break;
            case 2: settings.adjustWindow(-1); break;
            case 3: settings.adjustWindow(1); break;
            case 4: settings.adjustScale(-0.1F); break;
            case 5: settings.adjustScale(0.1F); break;
            case 6: settings.adjustX(-5); break;
            case 7: settings.adjustX(5); break;
            case 8: settings.adjustY(-5); break;
            case 9: settings.adjustY(5); break;
            case 10:
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
        int top = height / 2 - 116;
        drawCenteredString(fontRendererObj, "AutoSave", center, top, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "Recent save window: " + settings.getRecentSaveWindowSeconds() + " seconds",
                center, top + 176, 0xAAAAAA);
        drawCenteredString(fontRendererObj,
                "Position: " + settings.getX() + ", " + settings.getY()
                        + "   Scale: " + String.format(Locale.ROOT, "%.1f", settings.getScale()),
                center, top + 190, 0xAAAAAA);
        drawCenteredString(fontRendererObj, "Preview: World saved just now", center, top + 204, 0x55FF55);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String enabledLabel() { return "Enabled: " + (settings.isEnabled() ? "ON" : "OFF"); }
    private String hudLabel() { return "Show save state: " + (settings.isShowSaveState() ? "ON" : "OFF"); }
}
