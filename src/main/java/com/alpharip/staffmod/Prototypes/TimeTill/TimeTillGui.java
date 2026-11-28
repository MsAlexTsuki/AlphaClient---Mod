package com.alpharip.staffmod.TimeTill;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.Locale;

/** Standalone settings screen for the TimeTill HUD prototype. */
public final class TimeTillGui extends GuiScreen {
    private final GuiScreen parent;
    private final TimeTillSettings settings;
    private final timetillsunsetornight module;

    public TimeTillGui(GuiScreen parent, TimeTillSettings settings, timetillsunsetornight module) {
        this.parent = parent;
        this.settings = settings;
        this.module = module;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 82;

        buttonList.add(new GuiButton(0, center - 100, top, 200, 20, enabledText()));
        buttonList.add(new GuiButton(1, center - 100, top + 26, 200, 20, targetText()));
        buttonList.add(new GuiButton(2, center - 100, top + 52, 98, 20, "Scale -"));
        buttonList.add(new GuiButton(3, center + 2, top + 52, 98, 20, "Scale +"));
        buttonList.add(new GuiButton(4, center - 100, top + 78, 98, 20, "X - 5"));
        buttonList.add(new GuiButton(5, center + 2, top + 78, 98, 20, "X + 5"));
        buttonList.add(new GuiButton(6, center - 100, top + 104, 98, 20, "Y - 5"));
        buttonList.add(new GuiButton(7, center + 2, top + 104, 98, 20, "Y + 5"));
        buttonList.add(new GuiButton(8, center - 100, top + 136, 200, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: settings.setEnabled(!settings.isEnabled()); break;
            case 1: settings.toggleTarget(); break;
            case 2: settings.adjustScale(-0.1F); break;
            case 3: settings.adjustScale(0.1F); break;
            case 4: settings.adjustX(-5); break;
            case 5: settings.adjustX(5); break;
            case 6: settings.adjustY(-5); break;
            case 7: settings.adjustY(5); break;
            case 8:
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
        int top = height / 2 - 104;
        drawCenteredString(fontRendererObj, "TimeTill", center, top, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "Position: " + settings.getX() + ", " + settings.getY()
                        + "   Scale: " + String.format(Locale.ROOT, "%.1f", settings.getScale()),
                center, top + 158, 0xAAAAAA);
        drawCenteredString(fontRendererObj, "Counts down using the current world's time",
                center, top + 172, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String enabledText() {
        return "Enabled: " + (settings.isEnabled() ? "ON" : "OFF");
    }

    private String targetText() {
        return "Target: " + (settings.getTarget() == TimeTillSettings.Target.NIGHT ? "Night" : "Sunset");
    }
}
