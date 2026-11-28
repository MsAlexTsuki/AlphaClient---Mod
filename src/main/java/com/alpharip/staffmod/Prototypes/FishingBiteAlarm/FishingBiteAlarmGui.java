package com.alpharip.staffmod.fishingbitealarm;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Standalone settings screen for Fishing Bite Alarm. */
public final class FishingBiteAlarmGui extends GuiScreen {
    private final FishingBiteAlarm module;
    private final GuiScreen parent;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;

    public FishingBiteAlarmGui(FishingBiteAlarm module, GuiScreen parent) {
        if (module == null) throw new IllegalArgumentException("module");
        this.module = module;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        panelWidth = Math.min(340, Math.max(280, width - 20));
        int panelHeight = 218;
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        int fullWidth = panelWidth - 32;
        int halfWidth = (fullWidth - 8) / 2;
        int x = panelLeft + 16;

        addButton(1, x, panelTop + 48, fullWidth,
                "Alarm: " + onOff(module.getSettings().isEnabled()));
        addButton(2, x, panelTop + 76, halfWidth, "Interval: -5 sec");
        addButton(3, x + halfWidth + 8, panelTop + 76, halfWidth, "Interval: +5 sec");
        addButton(4, x, panelTop + 104, halfWidth, "Volume: -10%");
        addButton(5, x + halfWidth + 8, panelTop + 104, halfWidth, "Volume: +10%");
        addButton(6, x, panelTop + 132, fullWidth, "Test alarm sound");
        addButton(7, x, panelTop + 160, halfWidth, "Save");
        addButton(8, x + halfWidth + 8, panelTop + 160, halfWidth, "Done");
    }

    private void addButton(int id, int x, int y, int buttonWidth, String label) {
        buttonList.add(new GuiButton(id, x, y, buttonWidth, 20, label));
    }

    private String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        FishingBiteAlarmSettings settings = module.getSettings();
        switch (button.id) {
            case 1:
                settings.setEnabled(!settings.isEnabled());
                break;
            case 2:
                settings.adjustIntervalSeconds(-5);
                break;
            case 3:
                settings.adjustIntervalSeconds(5);
                break;
            case 4:
                settings.adjustVolume(-0.1F);
                break;
            case 5:
                settings.adjustVolume(0.1F);
                break;
            case 6:
                module.testAlarm();
                break;
            case 7:
                module.save();
                break;
            case 8:
                module.save();
                mc.displayGuiScreen(parent);
                return;
            default:
                return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Fishing Bite Alarm",
                width / 2, panelTop + 14, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "Detects a sudden downward bobber movement in water.",
                width / 2, panelTop + 31, 0xAAAAAA);
        FishingBiteAlarmSettings settings = module.getSettings();
        drawCenteredString(fontRendererObj,
                "Minimum alert interval: " + settings.getIntervalSeconds() + " seconds",
                width / 2, panelTop + 190, 0xCCCCCC);
        drawCenteredString(fontRendererObj,
                "Sound volume: " + Math.round(settings.getVolume() * 100.0F) + "%",
                width / 2, panelTop + 202, 0xCCCCCC);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
