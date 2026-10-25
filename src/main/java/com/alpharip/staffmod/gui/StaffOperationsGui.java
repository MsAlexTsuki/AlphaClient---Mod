package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import java.io.IOException;

/**
 * Staff Operations is the module-folder entry point. The dashboard is a separate screen.
 */
public final class StaffOperationsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int DASHBOARD = 10;
    private static final int CASES = 11;
    private static final int SHIFTS = 12;
    private static final int PRESENCE = 13;
    private static final int AUDIT = 14;
    private static final int NOTES = 15;
    private static final int ACCENT = 0xFFFFD866;

    private final net.minecraft.client.gui.GuiScreen parent;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int contentLeft, contentTop, contentRight;

    public StaffOperationsGui(net.minecraft.client.gui.GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!PermissionService.hasStaffModsAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        // Keep the established Staff Operations dimensions unchanged.
        panelWidth = Math.min(590, Math.max(360, width - 32));
        panelHeight = Math.min(350, Math.max(250, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        contentLeft = panelLeft + 16;
        contentRight = panelLeft + panelWidth - 16;
        contentTop = panelTop + 82;

        int gap = 8;
        int tileWidth = (contentRight - contentLeft - gap) / 2;
        int tileHeight = 34;
        buttonList.add(new AlphaButton(CASES, contentLeft, contentTop, tileWidth, tileHeight, "Cases"));
        buttonList.add(new AlphaButton(SHIFTS, contentLeft + tileWidth + gap, contentTop, tileWidth, tileHeight, "Shifts"));
        buttonList.add(new AlphaButton(PRESENCE, contentLeft, contentTop + tileHeight + gap, tileWidth, tileHeight, "Presence"));
        buttonList.add(new AlphaButton(AUDIT, contentLeft + tileWidth + gap, contentTop + tileHeight + gap, tileWidth, tileHeight, "Activity and audit"));
        buttonList.add(new AlphaButton(DASHBOARD, contentLeft, panelTop + panelHeight - 63,
                tileWidth, 22, "Open staff dashboard", true));
        buttonList.add(new AlphaButton(NOTES, contentLeft + tileWidth + gap, panelTop + panelHeight - 63,
                tileWidth, 22, "Private staff notes"));
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 94,
                panelTop + panelHeight - 31, 80, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (!PermissionService.hasStaffModsAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == DASHBOARD) {
            mc.displayGuiScreen(new StaffDashboardGui(this));
        } else if (button.id == CASES) {
            mc.displayGuiScreen(new StaffOperationsModuleGui(this, "case"));
        } else if (button.id == SHIFTS) {
            mc.displayGuiScreen(new StaffOperationsModuleGui(this, "shift"));
        } else if (button.id == PRESENCE) {
            mc.displayGuiScreen(new StaffOperationsModuleGui(this, "presence"));
        } else if (button.id == AUDIT) {
            mc.displayGuiScreen(new StaffOperationsModuleGui(this, "audit"));
        } else if (button.id == NOTES) {
            mc.displayGuiScreen(new PrivateStaffNotesGui(this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        int right = panelLeft + panelWidth;
        int bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xE00B0D12);
        drawRect(panelLeft, panelTop, right, panelTop + 1, ACCENT);
        drawRect(panelLeft + 1, panelTop + 1, panelLeft + 2, bottom - 1, 0x554B515D);
        drawRect(right - 1, panelTop + 1, right, bottom - 1, 0x554B515D);

        // Follow Developer -> Alpha Developer Studio's restrained workspace hierarchy.
        drawString(fontRendererObj, "Alpha  /  Staff operations", panelLeft + 16, panelTop + 13, 0xFFF0F1F4);
        drawString(fontRendererObj, "Staff information and management", panelLeft + 16, panelTop + 30, ACCENT);
        drawString(fontRendererObj, "Modules", panelLeft + 16, panelTop + 49, 0xFFB8BDC8);
        drawRect(panelLeft + 14, panelTop + 66, right - 14, panelTop + 67, 0x554B515D);
        drawString(fontRendererObj, "Open a staff module or launch the separate dashboard.", contentLeft,
                panelTop + 71, 0xFFE6E8ED);
        drawRect(panelLeft + 14, bottom - 36, right - 14, bottom - 35, 0x334B515D);
        drawString(fontRendererObj, "Staff operations  •  Module access", panelLeft + 14, bottom - 25, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
