// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.inventorysearch;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Compact AlphaClient settings screen for InventorySearch. */
public final class InventorySearchGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;

    private final GuiScreen parent;
    private int left, top, right, bottom;

    public InventorySearchGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        left = width / 2 - 130;
        right = left + 260;
        top = height / 2 - 56;
        bottom = top + 112;

        buttonList.add(new AlphaButton(
                TOGGLE, left + 20, top + 55, 220, 20,
                "SEARCH BAR: " + (InventorySearchSettings.isEnabled() ? "ON" : "OFF"),
                false, InventorySearchSettings.isEnabled()
        ));
        buttonList.add(new AlphaButton(BACK, left + 10, bottom - 25, 72, 18, "BACK"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, right, bottom, 0xE00A0C11);
        drawRect(left, top, right, top + 2, 0xFFFFD866);

        drawCenteredString(fontRendererObj, "INVENTORY SEARCH",
                width / 2, top + 15, 0xFFF3F4F6);
        drawCenteredString(fontRendererObj, "Search items in inventories and containers",
                width / 2, top + 34, 0xFFB8BDC8);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == TOGGLE) {
            InventorySearchSettings.setEnabled(!InventorySearchSettings.isEnabled());
            initGui();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
