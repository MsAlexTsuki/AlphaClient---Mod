// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.combat.AimAssistSettings;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Founder-only settings screen for configurable smooth AimAssist. */
public final class AimAssistSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLED = 10;
    private static final int RESET = 11;
    private static final int AREA = 12;

    private static final int CORRECTION_DOWN = 20, CORRECTION_UP = 21;
    private static final int DELAY_DOWN = 22, DELAY_UP = 23;
    private static final int ANGLE_DOWN = 24, ANGLE_UP = 25;
    private static final int HORIZONTAL_DOWN = 26, HORIZONTAL_UP = 27;
    private static final int VERTICAL_DOWN = 28, VERTICAL_UP = 29;

    private final GuiScreen parent;
    private int left, top, panelWidth, panelHeight;

    public AimAssistSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    private boolean hasFounderAccess() {
        return SupabaseCommunityService.isSignedInFounder();
    }

    private AlphaModule getAimAssistModule() {
        return AlphaModuleRegistry.find("aim-assist");
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!hasFounderAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        AlphaModule module = getAimAssistModule();
        if (module == null || !module.isImplemented() || !module.isUnlocked()) {
            mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(430, width - 12);
        panelHeight = Math.min(292, height - 12);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        int gap = 8;
        int colWidth = (panelWidth - 32 - gap) / 2;
        int leftCol = left + 12;
        int rightCol = leftCol + colWidth + gap;
        int y = top + 56;

        buttonList.add(new AlphaButton(ENABLED, width / 2 - 100, y, 200, 20,
                "AIMASSIST: " + (module.isConfiguredEnabled() ? "ON" : "OFF"),
                module.isConfiguredEnabled()));

        int rowY = top + 100;
        addStepper(CORRECTION_DOWN, CORRECTION_UP, leftCol, rowY, colWidth,
                "SPEED " + AimAssistSettings.getCorrectionSpeed() + "%");
        addStepper(DELAY_DOWN, DELAY_UP, leftCol, rowY + 34, colWidth,
                "DELAY " + AimAssistSettings.getReactionDelayMs() + "ms");
        addStepper(ANGLE_DOWN, ANGLE_UP, leftCol, rowY + 68, colWidth,
                "ANGLE " + AimAssistSettings.getMaximumAngleDegrees() + "deg");

        addStepper(HORIZONTAL_DOWN, HORIZONTAL_UP, rightCol, rowY, colWidth,
                "HORIZONTAL " + AimAssistSettings.getHorizontalSpeed() + "/20");
        addStepper(VERTICAL_DOWN, VERTICAL_UP, rightCol, rowY + 34, colWidth,
                "VERTICAL " + AimAssistSettings.getVerticalSpeed() + "/20");
        buttonList.add(new AlphaButton(AREA, rightCol, rowY + 68, colWidth, 20,
                "AREA: " + AimAssistSettings.getTargetArea().getLabel()));

        buttonList.add(new AlphaButton(RESET, left + 12, top + panelHeight - 31, 100, 18, "RESET DEFAULTS"));
        buttonList.add(new AlphaButton(BACK, left + panelWidth - 87, top + panelHeight - 31, 75, 18, "BACK"));
    }

    private void addStepper(int downId, int upId, int x, int y, int colWidth, String label) {
        int arrowWidth = 22;
        int valueWidth = colWidth - arrowWidth * 2 - 4;
        buttonList.add(new AlphaButton(downId, x, y, arrowWidth, 20, "-"));
        GuiButton value = new AlphaButton(1000 + downId, x + arrowWidth + 2, y, valueWidth, 20, label);
        value.enabled = false;
        buttonList.add(value);
        buttonList.add(new AlphaButton(upId, x + arrowWidth + valueWidth + 4, y, arrowWidth, 20, "+"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (!hasFounderAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        AlphaModule module = getAimAssistModule();
        if (module == null || !module.isImplemented() || !module.isUnlocked()) {
            mc.displayGuiScreen(parent);
            return;
        }

        switch (button.id) {
            case BACK:
                mc.displayGuiScreen(parent);
                return;
            case ENABLED:
                module.setEnabled(!module.isConfiguredEnabled());
                break;
            case RESET:
                AimAssistSettings.reset();
                break;
            case AREA:
                AimAssistSettings.cycleTargetArea();
                break;
            case CORRECTION_DOWN:
                AimAssistSettings.setCorrectionSpeed(AimAssistSettings.getCorrectionSpeed() - 1);
                break;
            case CORRECTION_UP:
                AimAssistSettings.setCorrectionSpeed(AimAssistSettings.getCorrectionSpeed() + 1);
                break;
            case DELAY_DOWN:
                AimAssistSettings.setReactionDelayMs(AimAssistSettings.getReactionDelayMs() - 25);
                break;
            case DELAY_UP:
                AimAssistSettings.setReactionDelayMs(AimAssistSettings.getReactionDelayMs() + 25);
                break;
            case ANGLE_DOWN:
                AimAssistSettings.setMaximumAngleDegrees(AimAssistSettings.getMaximumAngleDegrees() - 1);
                break;
            case ANGLE_UP:
                AimAssistSettings.setMaximumAngleDegrees(AimAssistSettings.getMaximumAngleDegrees() + 1);
                break;
            case HORIZONTAL_DOWN:
                AimAssistSettings.setHorizontalSpeed(AimAssistSettings.getHorizontalSpeed() - 1);
                break;
            case HORIZONTAL_UP:
                AimAssistSettings.setHorizontalSpeed(AimAssistSettings.getHorizontalSpeed() + 1);
                break;
            case VERTICAL_DOWN:
                AimAssistSettings.setVerticalSpeed(AimAssistSettings.getVerticalSpeed() - 1);
                break;
            case VERTICAL_UP:
                AimAssistSettings.setVerticalSpeed(AimAssistSettings.getVerticalSpeed() + 1);
                break;
            default:
                return;
        }
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xE00A0C11);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "AIMASSIST", width / 2, top + 12, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "FOUNDER SETTINGS  /  SMOOTH MODE", width / 2, top + 27, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Range locked to 3.2 blocks  |  Full-body targeting", width / 2, top + 40, 0xFFF0F1F4);
        drawString(fontRendererObj, "CORRECTION", left + 12, top + 88, 0xFFB8BDC8);
        drawString(fontRendererObj, "AXIS / TARGET", left + panelWidth / 2 + 4, top + 88, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "No target acquisition outside the selected angle", width / 2, top + 207, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Changes save automatically", width / 2, top + 220, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
