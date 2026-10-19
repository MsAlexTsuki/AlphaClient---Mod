package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/**
 * Dedicated W-Tap Assistance settings layout.
 * These controls are presentation-only until a later task connects them to configuration and input behavior.
 */
public final class WTapSettingsGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int ENABLED = 10;
    private static final int DELAY = 11;
    private static final int MODE = 12;
    private static final int TRIGGER = 13;
    private static final int TARGET = 14;
    private static final int COOLDOWN = 15;
    private static final int REQUIRE_W = 16;

    private static final String[] MODES = {"LEGIT", "STANDARD", "AGGRESSIVE", "MANUAL"};
    private static final String[] TRIGGERS = {"ON HIT", "ON ATTACK"};
    private static final String[] TARGETS = {"PLAYERS", "ALL LIVING ENTITIES"};

    private final GuiScreen parent;
    private boolean enabled;
    private boolean requireW = true;
    private int delay = 2;
    private int cooldown = 4;
    private int mode;
    private int trigger;
    private int target;

    public WTapSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int buttonWidth = Math.min(250, width - 24);
        int x = (width - buttonWidth) / 2;
        int gap = 25;
        int totalHeight = gap * 7;
        int y = Math.max(42, (height - totalHeight) / 2 - 4);

        buttonList.add(new AlphaButton(ENABLED, x, y, buttonWidth, 20, "ENABLED: " + onOff(enabled)));
        buttonList.add(new AlphaButton(DELAY, x, y + gap, buttonWidth, 20, "DELAY: " + delay + " TICKS"));
        buttonList.add(new AlphaButton(MODE, x, y + gap * 2, buttonWidth, 20, "MODE: " + MODES[mode]));
        buttonList.add(new AlphaButton(TRIGGER, x, y + gap * 3, buttonWidth, 20, "TRIGGER: " + TRIGGERS[trigger]));
        buttonList.add(new AlphaButton(TARGET, x, y + gap * 4, buttonWidth, 20, "TARGETS: " + TARGETS[target]));
        buttonList.add(new AlphaButton(COOLDOWN, x, y + gap * 5, buttonWidth, 20, "COOLDOWN: " + cooldown + " TICKS"));
        buttonList.add(new AlphaButton(REQUIRE_W, x, y + gap * 6, buttonWidth, 20, "REQUIRE W KEY: " + onOff(requireW)));
        buttonList.add(new AlphaButton(BACK, width / 2 - 55, height - 27, 110, 18, "BACK"));
    }

    private String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case BACK:
                mc.displayGuiScreen(parent);
                return;
            case ENABLED:
                enabled = !enabled;
                break;
            case DELAY:
                delay = delay >= 10 ? 0 : delay + 1;
                break;
            case MODE:
                mode = (mode + 1) % MODES.length;
                break;
            case TRIGGER:
                trigger = (trigger + 1) % TRIGGERS.length;
                break;
            case TARGET:
                target = (target + 1) % TARGETS.length;
                break;
            case COOLDOWN:
                cooldown = cooldown >= 20 ? 0 : cooldown + 1;
                break;
            case REQUIRE_W:
                requireW = !requireW;
                break;
            default:
                return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        int panelWidth = Math.min(292, width - 12);
        int panelHeight = Math.min(270, height - 12);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xD90A0C11);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFD866);
        drawRect(left, top + 2, left + 1, top + panelHeight, 0x554B515D);
        drawRect(left + panelWidth - 1, top + 2, left + panelWidth, top + panelHeight, 0x554B515D);
        String title = "W-TAP ASSISTANCE";
        drawString(fontRendererObj, title, width / 2 - fontRendererObj.getStringWidth(title) / 2,
                top + 10, 0xFFFFD866);
        String subtitle = "FOUNDER MODS";
        drawString(fontRendererObj, subtitle, width / 2 - fontRendererObj.getStringWidth(subtitle) / 2,
                top + 22, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
        String note = "GUI ONLY - SETTINGS NOT CONNECTED";
        drawString(fontRendererObj, note, width / 2 - fontRendererObj.getStringWidth(note) / 2,
                top + panelHeight - 12, 0xFFB8BDC8);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
