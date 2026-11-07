// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.movement;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Founder-only configuration screen for AutoStrafe. */
public final class AutoStrafeSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLED = 10;
    private static final int DIRECTION = 11;
    private static final int SPEED_DOWN = 12, SPEED_UP = 13;
    private static final int DISTANCE_DOWN = 14, DISTANCE_UP = 15;
    private static final int RANGE_DOWN = 16, RANGE_UP = 17;
    private static final int RESET = 18;

    private final GuiScreen parent;
    private int left, top, panelWidth, panelHeight;

    public AutoStrafeSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    private AlphaModule getModule() {
        return AlphaModuleRegistry.find("auto-strafe");
    }

    private boolean hasFounderAccess() {
        return SupabaseCommunityService.isSignedInFounder();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        AlphaModule module = getModule();
        if (!hasFounderAccess() || module == null || !module.isUnlocked()) {
            mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(360, width - 16);
        panelHeight = Math.min(260, height - 16);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        boolean settingsEnabled = module.isConfiguredEnabled();
        buttonList.add(new AlphaButton(ENABLED, width / 2 - 90, top + 48, 180, 20,
                "AUTOSTRAFE: " + (settingsEnabled ? "ON" : "OFF")));
        AlphaButton directionButton = new AlphaButton(DIRECTION, left + 16, top + 84,
                panelWidth - 32, 20, "DIRECTION: " + AutoStrafeSettings.getDirection().name());
        directionButton.enabled = settingsEnabled;
        buttonList.add(directionButton);
        addStepper(SPEED_DOWN, SPEED_UP, "STRAFE STRENGTH: " + AutoStrafeSettings.getSpeed() + "%", top + 112, settingsEnabled);
        addStepper(DISTANCE_DOWN, DISTANCE_UP,
                "ORBIT DISTANCE: " + format(AutoStrafeSettings.getOrbitDistance()) + " blocks", top + 142, settingsEnabled);
        addStepper(RANGE_DOWN, RANGE_UP,
                "TARGET RANGE: " + format(AutoStrafeSettings.getTargetRange()) + " blocks", top + 172, settingsEnabled);

        AlphaButton resetButton = new AlphaButton(RESET, left + 16, top + panelHeight - 30, 112, 18, "RESET DEFAULTS");
        resetButton.enabled = settingsEnabled;
        buttonList.add(resetButton);
        buttonList.add(new AlphaButton(BACK, left + panelWidth - 92, top + panelHeight - 30, 76, 18, "BACK"));
    }

    private void addStepper(int downId, int upId, String label, int y, boolean enabled) {
        int center = width / 2;
        AlphaButton down = new AlphaButton(downId, center - 142, y, 24, 20, "-");
        AlphaButton value = new AlphaButton(1000 + downId, center - 114, y, 228, 20, label);
        AlphaButton up = new AlphaButton(upId, center + 118, y, 24, 20, "+");
        down.enabled = enabled;
        value.enabled = enabled;
        up.enabled = enabled;
        buttonList.add(down);
        buttonList.add(value);
        buttonList.add(up);
    }

    private String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (!hasFounderAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        AlphaModule module = getModule();
        if (module == null || !module.isUnlocked()) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (!module.isConfiguredEnabled() && button.id != ENABLED && button.id != BACK) return;

        switch (button.id) {
            case ENABLED:
                module.setEnabled(!module.isConfiguredEnabled());
                break;
            case DIRECTION:
                AutoStrafeSettings.cycleDirection();
                break;
            case SPEED_DOWN:
                AutoStrafeSettings.setSpeed(AutoStrafeSettings.getSpeed() - 5);
                break;
            case SPEED_UP:
                AutoStrafeSettings.setSpeed(AutoStrafeSettings.getSpeed() + 5);
                break;
            case DISTANCE_DOWN:
                AutoStrafeSettings.setOrbitDistance(AutoStrafeSettings.getOrbitDistance() - 0.25D);
                break;
            case DISTANCE_UP:
                AutoStrafeSettings.setOrbitDistance(AutoStrafeSettings.getOrbitDistance() + 0.25D);
                break;
            case RANGE_DOWN:
                AutoStrafeSettings.setTargetRange(AutoStrafeSettings.getTargetRange() - 0.5D);
                break;
            case RANGE_UP:
                AutoStrafeSettings.setTargetRange(AutoStrafeSettings.getTargetRange() + 0.5D);
                break;
            case RESET:
                AutoStrafeSettings.reset();
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
        drawCenteredString(fontRendererObj, "AUTOSTRAFE", width / 2, top + 12, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "FOUNDER SETTINGS", width / 2, top + 27, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Steers around the nearest visible player while you move.",
                width / 2, top + 207, 0xFFF0F1F4);
        drawCenteredString(fontRendererObj, "Settings are saved automatically.",
                width / 2, top + 220, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
