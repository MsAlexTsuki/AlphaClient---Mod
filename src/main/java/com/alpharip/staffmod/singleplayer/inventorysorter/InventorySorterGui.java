// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.inventorysorter;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Full singleplayer control and usage screen for Inventory Sorter. */
public final class InventorySorterGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int ENABLE = 2;
    private static final int ACCENT = 0xFFFFC83D;

    private final GuiScreen parent;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public InventorySorterGui(GuiScreen parent) {
        this.parent = parent;
    }

    private boolean isAvailable() {
        return InventorySorterAccess.isAvailable(Minecraft.getMinecraft());
    }

    private AlphaModuleFeature getFeature() {
        return AlphaModuleRegistry.findFeature("inventory-sorter");
    }

    private boolean isEnabled() {
        AlphaModuleFeature feature = getFeature();
        return feature != null && feature.isEnabled();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!isAvailable()) {
            if (mc != null) mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(420, width - 24);
        panelHeight = 232;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        buttonList.add(new AlphaButton(ENABLE, left + 16, top + 54,
                panelWidth - 32, 22, "Inventory Sorter: " + (isEnabled() ? "ON" : "OFF"),
                isEnabled()));
        buttonList.add(new AlphaButton(BACK, left + 16, top + panelHeight - 30,
                90, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (!isAvailable()) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == ENABLE) {
            AlphaModuleFeature feature = getFeature();
            if (feature != null) feature.setEnabled(!feature.isEnabled());
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!isAvailable()) {
            mc.displayGuiScreen(parent);
            return;
        }

        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);

        drawCenteredString(fontRendererObj, "INVENTORY SORTER", width / 2,
                top + 15, 0xFFF2F3F5);
        drawCenteredString(fontRendererObj, "SINGLEPLAYER TOOL", width / 2,
                top + 32, ACCENT);

        drawString(fontRendererObj, "CHEST CONTROLS", left + 16, top + 88, 0xFFF2F3F5);
        drawString(fontRendererObj, "Grab All  - Transfer chest items into available inventory space",
                left + 16, top + 103, 0xFFB8BDC8);
        drawString(fontRendererObj, "Sort Chest  - Sort the open chest contents",
                left + 16, top + 117, 0xFFB8BDC8);
        drawString(fontRendererObj, "Sort Inv.  - Sort player inventory slots, excluding the hotbar",
                left + 16, top + 131, 0xFFB8BDC8);

        drawString(fontRendererObj, "MIDDLE-CLICK", left + 16, top + 155, 0xFFF2F3F5);
        drawString(fontRendererObj, "Middle-click a chest or main-inventory slot to sort that section.",
                left + 16, top + 170, 0xFFB8BDC8);
        drawString(fontRendererObj, "Sorting cycles: Alphabetical, Item type + name, Amount (most first).",
                left + 16, top + 184, 0xFFB8BDC8);
        drawString(fontRendererObj, "No automatic sorting. The hotbar is never sorted.",
                left + 16, top + 198, 0xFFB8BDC8);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
