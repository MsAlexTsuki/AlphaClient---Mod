// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.quality.anvilcost.AnvilCostMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Settings screen for the Anvil Cost Quality module. */
public final class AnvilCostSettingsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;

    private final GuiScreen parent;
    private int left;
    private int top;
    private int right;
    private int bottom;

    public AnvilCostSettingsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        left = width / 2 - 160;
        right = left + 320;
        top = Math.max(12, height / 2 - 90);
        bottom = Math.min(height - 12, top + 180);

        buttonList.add(new AlphaButton(TOGGLE, left + 20, top + 62, 280, 20,
                "ANVIL COST: " + (AnvilCostMod.isEnabled() ? "ON" : "OFF"),
                AnvilCostMod.isEnabled()));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 27, 75, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);
        drawCenteredString(fontRendererObj, "ANVIL COST", width / 2, top + 18, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Preview anvil levels and material requirements",
                width / 2, top + 38, 0xFFB8BDC8);
        drawCenteredString(fontRendererObj, "in item tooltips while an inventory is open.",
                width / 2, top + 49, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == TOGGLE) {
            AnvilCostMod.setEnabled(!AnvilCostMod.isEnabled());
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
