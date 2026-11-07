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
        panelHeight = Math.min(200, height - 16);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        boolean settingsEnabled = module.isConfiguredEnabled();
        buttonList.add(new AlphaButton(ENABLED, width / 2 - 90, top + 48, 180, 20,
                "AUTOSTRAFE: " + (settingsEnabled ? "ON" : "OFF")));

        int center = width / 2;
        AlphaButton rangeDown = new AlphaButton(RANGE_DOWN, center - 142, top + 84, 24, 20, "-");
        AlphaButton rangeValue = new AlphaButton(1016, center - 114, top + 84, 228, 20,
                "TARGET RANGE: " + format(AutoStrafeSettings.getTargetRange()) + " blocks");
        AlphaButton rangeUp = new AlphaButton(RANGE_UP, center + 118, top + 84, 24, 20, "+");
        rangeDown.enabled = settingsEnabled;
        rangeValue.enabled = settingsEnabled;
        rangeUp.enabled = settingsEnabled;
        buttonList.add(rangeDown);
        buttonList.add(rangeValue);
        buttonList.add(rangeUp);

        AlphaButton resetButton = new AlphaButton(RESET, left + 16, top + panelHeight - 30,
                112, 18, "RESET DEFAULTS");
        resetButton.enabled = settingsEnabled;
        buttonList.add(resetButton);
        buttonList.add(new AlphaButton(BACK, left + panelWidth - 92,
                top + panelHeight - 30, 76, 18, "BACK"));
    }

    private String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
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
        drawCenteredString(fontRendererObj, "Keeps W active with short, alternating A/D bursts.",
                width / 2, top + 125, 0xFFF0F1F4);
        drawCenteredString(fontRendererObj, "Pressing S manually always overrides AutoStrafe.",
                width / 2, top + 139, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
