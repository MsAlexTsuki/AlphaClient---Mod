// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Settings screen for the optional local Name Vieuwer display. */
public final class NameViewerSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;
    private static final int ACCENT = 0xFFFFD866;
    private final GuiScreen parent;
    private int left, top, right, bottom;

    public NameViewerSettingsGui(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        left = Math.max(12, width / 2 - 150);
        right = Math.min(width - 12, left + 300);
        top = Math.max(12, height / 2 - 90);
        bottom = Math.min(height - 12, top + 180);
        AlphaModule module = AlphaModuleRegistry.find("name-viewer");
        boolean enabled = module != null && module.isEnabled();
        buttonList.add(new AlphaButton(TOGGLE, left + 35, top + 105, right - left - 70, 22,
                enabled ? "DISABLE NAME VIEWER" : "ENABLE NAME VIEWER", true));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 28, 70, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x18000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, ACCENT);
        drawCenteredString(fontRendererObj, "NAME VIEWER", width / 2, top + 16, 0xFFF3F4F6);
        AlphaModule module = AlphaModuleRegistry.find("name-viewer");
        boolean enabled = module != null && module.isEnabled();
        drawCenteredString(fontRendererObj, "Status: " + (module == null ? "UNAVAILABLE" : module.getStatusLabel()),
                width / 2, top + 45, enabled ? 0xFF9CE2AC : 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Controls the extra name and rank display above your own player.",
                width / 2, top + 64, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Other players' rank badges remain enabled automatically.",
                width / 2, top + 78, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (button.id == TOGGLE) {
            AlphaModule module = AlphaModuleRegistry.find("name-viewer");
            if (module != null && module.isImplemented() && module.isUnlocked()) {
                module.setEnabled(!module.isEnabled());
            }
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}