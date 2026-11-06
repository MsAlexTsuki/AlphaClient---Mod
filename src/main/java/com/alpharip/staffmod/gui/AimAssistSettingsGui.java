// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.combat.AimAssistSettings;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;

/** Founder-only settings screen for AimAssist and its expansion profiles. */
public final class AimAssistSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLED = 10;
    private static final int RESET = 11;
    private static final int AREA = 12;
    private static final int MODE = 13;
    private static final int PRIORITY = 14;
    private static final int PREDICTION_DOWN = 30, PREDICTION_UP = 31;

    private static final int CORRECTION_DOWN = 20, CORRECTION_UP = 21;
    private static final int DELAY_DOWN = 22, DELAY_UP = 23;
    private static final int ANGLE_DOWN = 24, ANGLE_UP = 25;
    private static final int HORIZONTAL_DOWN = 26, HORIZONTAL_UP = 27;
    private static final int VERTICAL_DOWN = 28, VERTICAL_UP = 29;

    private final net.minecraft.client.gui.GuiScreen parent;
    private int left, top, panelWidth, panelHeight;

    public AimAssistSettingsGui(net.minecraft.client.gui.GuiScreen parent) {
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
        panelHeight = Math.min(350, height - 12);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        int gap = 8;
        int colWidth = (panelWidth - 32 - gap) / 2;
        int leftCol = left + 12;
        int rightCol = leftCol + colWidth + gap;
        int y = top + 56;
        boolean settingsEnabled = module.isConfiguredEnabled();

        buttonList.add(new AlphaButton(ENABLED, width / 2 - 100, y, 200, 20,
                "AIMASSIST: " + (settingsEnabled ? "ON" : "OFF"), settingsEnabled));

        int rowY = top + 100;
        addStepper(CORRECTION_DOWN, CORRECTION_UP, leftCol, rowY, colWidth,
                "SPEED " + AimAssistSettings.getCorrectionSpeed() + "%", settingsEnabled);
        addStepper(DELAY_DOWN, DELAY_UP, leftCol, rowY + 34, colWidth,
                "DELAY " + AimAssistSettings.getReactionDelayMs() + "ms", settingsEnabled);
        addStepper(ANGLE_DOWN, ANGLE_UP, leftCol, rowY + 68, colWidth,
                "ANGLE " + AimAssistSettings.getMaximumAngleDegrees() + "deg", settingsEnabled);

        addStepper(HORIZONTAL_DOWN, HORIZONTAL_UP, rightCol, rowY, colWidth,
                "HORIZONTAL " + AimAssistSettings.getHorizontalSpeed() + "/20", settingsEnabled);
        addStepper(VERTICAL_DOWN, VERTICAL_UP, rightCol, rowY + 34, colWidth,
                "VERTICAL " + AimAssistSettings.getVerticalSpeed() + "/20", settingsEnabled);
        AlphaButton areaButton = new AlphaButton(AREA, rightCol, rowY + 68, colWidth, 20,
                "AREA: " + AimAssistSettings.getTargetArea().getLabel(), settingsEnabled);
        areaButton.enabled = settingsEnabled;
        buttonList.add(areaButton);

        AlphaButton modeButton = new AlphaButton(MODE, left + 12, top + 210,
                panelWidth - 24, 20, "MODE: " + AimAssistSettings.getMode().name(), settingsEnabled);
        modeButton.enabled = settingsEnabled;
        buttonList.add(modeButton);

        AlphaButton priorityButton = new AlphaButton(PRIORITY, left + 12, top + 236,
                panelWidth - 24, 20, "TARGET PRIORITY: " + AimAssistSettings.getTargetPriority().name(),
                settingsEnabled);
        priorityButton.enabled = settingsEnabled;
        buttonList.add(priorityButton);

        addStepper(PREDICTION_DOWN, PREDICTION_UP, left + 12, top + 262,
                panelWidth - 24, "PREDICTION " + AimAssistSettings.getPredictionTicks() + " TICKS",
                settingsEnabled);

        AlphaButton resetButton = new AlphaButton(RESET, left + 12, top + panelHeight - 31,
                100, 18, "RESET DEFAULTS");
        resetButton.enabled = settingsEnabled;
        buttonList.add(resetButton);
        buttonList.add(new AlphaButton(BACK, left + panelWidth - 87,
                top + panelHeight - 31, 75, 18, "BACK"));
    }

    private void addStepper(int downId, int upId, int x, int y, int colWidth,
                            String label, boolean settingsEnabled) {
        int arrowWidth = 22;
        int valueWidth = colWidth - arrowWidth * 2 - 4;
        AlphaButton downButton = new AlphaButton(downId, x, y, arrowWidth, 20, "-");
        downButton.enabled = settingsEnabled;
        buttonList.add(downButton);
        AlphaButton valueButton = new AlphaButton(1000 + downId, x + arrowWidth + 2, y,
                valueWidth, 20, label, settingsEnabled);
        valueButton.enabled = settingsEnabled;
        buttonList.add(valueButton);
        AlphaButton upButton = new AlphaButton(upId, x + arrowWidth + valueWidth + 4, y,
                arrowWidth, 20, "+");
        upButton.enabled = settingsEnabled;
        buttonList.add(upButton);
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
        if (!module.isConfiguredEnabled() && button.id != ENABLED && button.id != BACK) return;

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
            case MODE:
                AimAssistSettings.cycleMode();
                break;
            case PRIORITY:
                AimAssistSettings.cycleTargetPriority();
                break;
            case PREDICTION_DOWN:
                AimAssistSettings.setPredictionTicks(AimAssistSettings.getPredictionTicks() - 0.5F);
                break;
            case PREDICTION_UP:
                AimAssistSettings.setPredictionTicks(AimAssistSettings.getPredictionTicks() + 0.5F);
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
        drawCenteredString(fontRendererObj, "FOUNDER SETTINGS  /  EXPANSION PROFILES",
                width / 2, top + 27, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Range locked to 3.2 blocks  |  Existing body-area targeting retained",
                width / 2, top + 40, 0xFFF0F1F4);
        drawString(fontRendererObj, "CORRECTION", left + 12, top + 88, 0xFFB8BDC8);
        drawString(fontRendererObj, "AXIS / TARGET", left + panelWidth / 2 + 4, top + 88, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Prediction applies only to Predictive mode",
                width / 2, top + 292, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Changes save automatically",
                width / 2, top + 305, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
