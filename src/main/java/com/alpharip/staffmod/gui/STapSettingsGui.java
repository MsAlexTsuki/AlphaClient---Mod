package com.alpharip.staffmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import com.alpharip.staffmod.movement.STapAssistance;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import java.io.IOException;

/** Founder-only S-Tap settings; mirrors W-Tap settings but controls the S/back key. */
public final class STapSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLED = 10;
    private static final int DELAY = 11;
    private static final int MODE = 12;
    private static final int TRIGGER = 13;
    private static final int TARGET = 14;
    private static final int COOLDOWN = 15;
    private static final int REQUIRE_S = 16;
    private static final String[] MODES = {"LEGIT", "STANDARD", "AGGRESSIVE", "MANUAL"};
    private static final String[] TRIGGERS = {"ON HIT", "ON ATTACK"};
    private static final String[] TARGETS = {"PLAYERS", "ALL LIVING ENTITIES"};

    private final GuiScreen parent;

    public STapSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        if (!hasFounderAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        buttonList.clear();
        int buttonWidth = Math.min(250, width - 24);
        int x = (width - buttonWidth) / 2;
        int gap = 23;
        int y = Math.max(40, (height - 190) / 2);

        buttonList.add(new AlphaButton(ENABLED, x, y, buttonWidth, 20, "ENABLED: " + onOff(STapAssistance.isEnabled())));
        buttonList.add(new AlphaButton(MODE, x, y + gap, buttonWidth, 20, "MODE: " + MODES[STapAssistance.getMode()]));
        buttonList.add(new AlphaButton(DELAY, x, y + gap * 2, buttonWidth, 20, "DELAY: " + STapAssistance.getDelay() + " TICKS"));
        buttonList.add(new AlphaButton(TRIGGER, x, y + gap * 3, buttonWidth, 20, "TRIGGER: " + TRIGGERS[STapAssistance.getTrigger()]));
        buttonList.add(new AlphaButton(TARGET, x, y + gap * 4, buttonWidth, 20, "TARGETS: " + TARGETS[STapAssistance.getTarget()]));
        buttonList.add(new AlphaButton(COOLDOWN, x, y + gap * 5, buttonWidth, 20, "COOLDOWN: " + STapAssistance.getCooldown() + " TICKS"));
        buttonList.add(new AlphaButton(REQUIRE_S, x, y + gap * 6, buttonWidth, 20, "REQUIRE S KEY: " + onOff(STapAssistance.isRequireS())));
        buttonList.add(new AlphaButton(BACK, width / 2 - 55, height - 27, 110, 18, "BACK"));
    }

    private boolean hasFounderAccess() {
        return PermissionService.hasAccess(PermissionService.Rank.FOUNDER);
    }

    private String onOff(boolean value) { return value ? "ON" : "OFF"; }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (!hasFounderAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        switch (button.id) {
            case BACK: mc.displayGuiScreen(parent); return;
            case ENABLED:
                STapAssistance.setEnabled(!STapAssistance.isEnabled());
                if (AlphaModuleRegistry.find("s-tap-assistance") != null) {
                    AlphaModuleRegistry.find("s-tap-assistance").setEnabled(STapAssistance.isEnabled());
                }
                break;
            case DELAY: STapAssistance.setDelay(STapAssistance.getDelay() >= 10 ? 0 : STapAssistance.getDelay() + 1); break;
            case MODE: STapAssistance.setMode((STapAssistance.getMode() + 1) % MODES.length); break;
            case TRIGGER: STapAssistance.setTrigger((STapAssistance.getTrigger() + 1) % TRIGGERS.length); break;
            case TARGET: STapAssistance.setTarget((STapAssistance.getTarget() + 1) % TARGETS.length); break;
            case COOLDOWN: STapAssistance.setCooldown(STapAssistance.getCooldown() >= 20 ? 0 : STapAssistance.getCooldown() + 1); break;
            case REQUIRE_S: STapAssistance.setRequireS(!STapAssistance.isRequireS()); break;
            default: return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        int panelWidth = Math.min(292, width - 12);
        int panelHeight = Math.min(250, height - 12);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xD90A0C11);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFD866);
        drawRect(left, top + 2, left + 1, top + panelHeight, 0x554B515D);
        drawRect(left + panelWidth - 1, top + 2, left + panelWidth, top + panelHeight, 0x554B515D);
        String title = "S-TAP";
        drawString(fontRendererObj, title, width / 2 - fontRendererObj.getStringWidth(title) / 2, top + 10, 0xFFFFD866);
        String subtitle = "FOUNDER ONLY";
        drawString(fontRendererObj, subtitle, width / 2 - fontRendererObj.getStringWidth(subtitle) / 2, top + 22, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
        String note = STapAssistance.isEnabled() ? "S-TAP ACTIVE" : "S-TAP DISABLED";
        drawString(fontRendererObj, note, width / 2 - fontRendererObj.getStringWidth(note) / 2, top + panelHeight - 12, 0xFFB8BDC8);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
