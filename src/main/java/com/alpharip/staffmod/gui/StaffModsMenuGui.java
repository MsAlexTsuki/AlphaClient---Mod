package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/**
 * Dedicated StaffMods interface. This is intentionally independent from the
 * retired StaffModsGui implementation.
 */
public final class StaffModsMenuGui extends GuiScreen {
    private static final int BACK = 1;
    private static final int CATEGORY_BASE = 20;
    private static final int MODULE_BASE = 50;
    private static final int ACCENT = 0xFFFFD866;

    private static final PermissionService.Rank[] RANKS = {
        PermissionService.Rank.PUBLIC,
        PermissionService.Rank.T_MOD,
        PermissionService.Rank.MOD,
        PermissionService.Rank.SR_MOD,
        PermissionService.Rank.ADMIN,
        PermissionService.Rank.SR_ADMIN,
        PermissionService.Rank.MANAGEMENT,
        PermissionService.Rank.OWNER,
        PermissionService.Rank.FOUNDER
    };
    private static final String[] LABELS = {
        "GENERAL", "T-MOD", "MOD", "SR MOD", "ADMIN",
        "SR ADMIN", "MANAGEMENT", "OWNER", "FOUNDER"
    };

    private final GuiScreen parent;
    private PermissionService.Rank selectedRank = PermissionService.Rank.PUBLIC;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int categoryWidth, contentLeft, contentTop, contentRight, contentBottom;

    public StaffModsMenuGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        panelWidth = Math.min(560, Math.max(360, width - 32));
        panelHeight = Math.min(330, Math.max(230, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        categoryWidth = Math.min(112, Math.max(88, panelWidth / 4));
        contentLeft = panelLeft + categoryWidth + 14;
        contentRight = panelLeft + panelWidth - 14;
        contentTop = panelTop + 68;
        contentBottom = panelTop + panelHeight - 42;

        int categoryY = panelTop + 76;
        for (int i = 0; i < RANKS.length; i++) {
            PermissionService.Rank rank = RANKS[i];
            if (rank != PermissionService.Rank.PUBLIC && !PermissionService.hasAccess(rank)) {
                continue;
            }
            buttonList.add(new AlphaButton(CATEGORY_BASE + i, panelLeft + 8, categoryY,
                    categoryWidth - 16, 22, LABELS[i], selectedRank == rank));
            categoryY += 26;
        }

        if (selectedRank == PermissionService.Rank.PUBLIC) {
            int tileWidth = Math.max(110, (contentRight - contentLeft - 10) / 2);
            buttonList.add(new AlphaButton(MODULE_BASE, contentLeft, contentTop,
                    tileWidth, 36, "BHOP"));
            buttonList.add(new AlphaButton(MODULE_BASE + 1, contentLeft + tileWidth + 10,
                    contentTop, tileWidth, 36, "UTILITIES"));
        }

        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 94,
                panelTop + panelHeight - 31, 80, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;

        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (button.id >= CATEGORY_BASE && button.id < CATEGORY_BASE + RANKS.length) {
            PermissionService.Rank rank = RANKS[button.id - CATEGORY_BASE];
            if (rank == PermissionService.Rank.PUBLIC || PermissionService.hasAccess(rank)) {
                selectedRank = rank;
                initGui();
            }
            return;
        }

        if (button.id == MODULE_BASE && selectedRank == PermissionService.Rank.PUBLIC) {
            mc.displayGuiScreen(new BhopSettingsGui(this));
        }
        // Utilities is shown as a category tile; no separate Utilities screen
        // is registered in the current project, so it does not open a fake GUI.
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);

        int right = panelLeft + panelWidth;
        int bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xE00B0D12);
        drawRect(panelLeft, panelTop, right, panelTop + 1, ACCENT);
        drawRect(panelLeft, panelTop + 1, panelLeft + 1, bottom - 1, 0x554B515D);
        drawRect(right - 1, panelTop + 1, right, bottom - 1, 0x554B515D);
        drawRect(panelLeft + categoryWidth + 5, panelTop + 54,
                panelLeft + categoryWidth + 6, bottom - 34, 0x554B515D);

        drawString(fontRendererObj, "ALPHA CLIENT", panelLeft + 14, panelTop + 14, 0xFFF0F1F4);
        drawString(fontRendererObj, "STAFFMODS", panelLeft + 14, panelTop + 34, ACCENT);
        drawString(fontRendererObj, "CATEGORIES", panelLeft + 14, panelTop + 58, 0xFFB8BDC8);
        drawString(fontRendererObj, LABELS[indexOf(selectedRank)], contentLeft,
                panelTop + 47, ACCENT);

        if (selectedRank == PermissionService.Rank.PUBLIC) {
            drawModuleCard(contentLeft, contentTop, Math.max(110, (contentRight - contentLeft - 10) / 2),
                    36, "BHOP", "Movement module", mouseX, mouseY);
            int tileWidth = Math.max(110, (contentRight - contentLeft - 10) / 2);
            drawModuleCard(contentLeft + tileWidth + 10, contentTop, tileWidth,
                    36, "UTILITIES", "Staff utilities", mouseX, mouseY);
            drawString(fontRendererObj, "General staff modules", contentLeft,
                    contentTop + 54, 0xFFB8BDC8);
        } else {
            drawString(fontRendererObj, "No modules registered for this rank yet.",
                    contentLeft, contentTop + 8, 0xFFB8BDC8);
        }

        drawString(fontRendererObj, "RANK ACCESS: " + PermissionService.getEffectiveRank().name(),
                contentLeft, bottom - 22, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawModuleCard(int x, int y, int w, int h, String title,
                                String subtitle, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int background = hovered ? 0xFF252A33 : 0xD9161820;
        int border = hovered ? ACCENT : 0x664B515D;
        drawRect(x, y, x + w, y + h, background);
        drawRect(x, y, x + w, y + 1, border);
        drawRect(x, y + h - 1, x + w, y + h, border);
        drawRect(x, y, x + 1, y + h, border);
        drawRect(x + w - 1, y, x + w, y + h, border);
        drawString(fontRendererObj, title, x + 9, y + 6, hovered ? ACCENT : 0xFFF0F1F4);
        drawString(fontRendererObj, subtitle, x + 9, y + 20, 0xFFB8BDC8);
    }

    private int indexOf(PermissionService.Rank rank) {
        for (int i = 0; i < RANKS.length; i++) {
            if (RANKS[i] == rank) return i;
        }
        return 0;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}