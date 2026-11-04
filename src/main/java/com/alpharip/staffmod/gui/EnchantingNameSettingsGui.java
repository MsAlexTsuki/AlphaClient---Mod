// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.quality.enchantingname.EnchantingNameMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Settings screen for the Enchanting Names Quality feature. */
public final class EnchantingNameSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLE = 2;

    private final GuiScreen parent;
    private int left;
    private int top;
    private int right;
    private int bottom;

    public EnchantingNameSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 160;
        right = left + 320;
        top = Math.max(12, height / 2 - 90);
        bottom = Math.min(height - 12, top + 180);

        boolean enabled = EnchantingNameMod.isEnabled();
        buttonList.add(new AlphaButton(ENABLE, left + 20, top + 66, 280, 20,
                "ENCHANTING NAMES: " + (enabled ? "ON" : "OFF"), enabled));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ENCHANTING NAMES",
                width / 2, top + 18, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Show localized enchantment names in the enchanting table.",
                width / 2, top + 40, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "Additional display options can be added here later.",
                width / 2, top + 51, 0xFF8E95A3);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == ENABLE) {
            EnchantingNameMod.setEnabled(!EnchantingNameMod.isEnabled());
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
