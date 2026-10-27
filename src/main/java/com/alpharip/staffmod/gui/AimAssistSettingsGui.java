// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Founder-only settings screen for the smooth AimAssist module. */
public final class AimAssistSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLED = 10;

    private final GuiScreen parent;

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

        int buttonWidth = Math.min(250, width - 24);
        int x = (width - buttonWidth) / 2;
        int y = Math.max(54, (height - 70) / 2);
        AlphaModule module = getAimAssistModule();

        if (module == null || !module.isImplemented() || !module.isUnlocked()) {
            mc.displayGuiScreen(parent);
            return;
        }

        buttonList.add(new AlphaButton(ENABLED, x, y, buttonWidth, 20,
                "AIMASSIST: " + (module.isConfiguredEnabled() ? "ON" : "OFF")));
        buttonList.add(new AlphaButton(BACK, width / 2 - 55, height - 28, 110, 18, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (!hasFounderAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (button.id == ENABLED) {
            AlphaModule module = getAimAssistModule();
            if (module == null || !module.isImplemented() || !module.isUnlocked()) {
                mc.displayGuiScreen(parent);
                return;
            }
            module.setEnabled(!module.isConfiguredEnabled());
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        int panelWidth = Math.min(292, width - 12);
        int panelHeight = Math.min(190, height - 12);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;

        drawRect(left, top, left + panelWidth, top + panelHeight, 0xD90A0C11);
        drawRect(left, top, left + panelWidth, top + 2, 0xFFFFD866);
        drawRect(left, top + 2, left + 1, top + panelHeight, 0x554B515D);
        drawRect(left + panelWidth - 1, top + 2, left + panelWidth, top + panelHeight, 0x554B515D);

        String title = "AIMASSIST";
        drawString(fontRendererObj, title,
                width / 2 - fontRendererObj.getStringWidth(title) / 2, top + 12, 0xFFFFD866);
        String subtitle = "FOUNDER MODS";
        drawString(fontRendererObj, subtitle,
                width / 2 - fontRendererObj.getStringWidth(subtitle) / 2, top + 25, 0xFFB8BDC8);
        String behavior = "Smooth aim correction";
        drawString(fontRendererObj, behavior,
                width / 2 - fontRendererObj.getStringWidth(behavior) / 2, top + 48, 0xFFF0F1F4);
        String detail = "Visible players close to crosshair only";
        drawString(fontRendererObj, detail,
                width / 2 - fontRendererObj.getStringWidth(detail) / 2, top + 60, 0xFFB8BDC8);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
