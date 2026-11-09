// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.inventorysorter;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.modules.AlphaModuleFeature;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Full singleplayer information screen for Inventory Sorter. */
public final class InventorySorterGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int TOGGLE = 2;
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

    @Override
    public void initGui() {
        buttonList.clear();
        if (!isAvailable()) {
            if (mc != null) mc.displayGuiScreen(parent);
            return;
        }

        panelWidth = Math.min(420, width - 24);
        panelHeight = 250;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;

        buttonList.add(new AlphaButton(BACK, left + 16, top + panelHeight - 30,
                90, 20, "BACK"));
        AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("inventory-sorter");
        boolean enabled = feature != null && feature.isEnabled();
        buttonList.add(new AlphaButton(TOGGLE, left + panelWidth - 126,
                top + panelHeight - 30, 110, 20, enabled ? "ON" : "OFF", enabled));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) return;
        if (button.id == BACK || !isAvailable()) {
            mc.displayGuiScreen(parent);
        } else if (button.id == TOGGLE) {
            AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("inventory-sorter");
            if (feature != null) feature.setEnabled(!feature.isConfiguredEnabled());
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
        AlphaModuleFeature feature = AlphaModuleRegistry.findFeature("inventory-sorter");
        boolean enabled = feature != null && feature.isEnabled();
        drawCenteredString(fontRendererObj, "STATUS: " + (enabled ? "ON" : "OFF"),
                width / 2, top + 47, enabled ? 0xFF75D889 : 0xFFFF7777);

        drawString(fontRendererObj, "CHEST CONTROLS", left + 16, top + 65, 0xFFF2F3F5);
        drawString(fontRendererObj, "Quick Grab transfers chest items into available inventory space.",
                left + 16, top + 82, 0xFFB8BDC8);
        drawString(fontRendererObj, "Sort Chest sorts the open chest contents.",
                left + 16, top + 97, 0xFFB8BDC8);
        drawString(fontRendererObj, "Sort Inventory sorts the main inventory, excluding the hotbar.",
                left + 16, top + 112, 0xFFB8BDC8);
        drawString(fontRendererObj, "Deposit All moves main-inventory items into the open chest.",
                left + 16, top + 127, 0xFFB8BDC8);

        drawString(fontRendererObj, "MIDDLE-CLICK", left + 16, top + 155, 0xFFF2F3F5);
        drawString(fontRendererObj, "Middle-click a chest slot or main-inventory slot to sort that section.",
                left + 16, top + 172, 0xFFB8BDC8);
        drawString(fontRendererObj, "Chest slots sort the chest; player slots sort the main inventory.",
                left + 16, top + 187, 0xFFB8BDC8);
        drawString(fontRendererObj, "Vanilla double-click item collection remains available.",
                left + 16, top + 208, 0xFFB8BDC8);
        drawString(fontRendererObj, "No automatic sorting or hotbar sorting.",
                left + 16, top + 223, 0xFFB8BDC8);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
