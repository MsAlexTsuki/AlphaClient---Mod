package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Rank-filtered entry point for staff tools. Rank entitlements fail closed online. */
public final class StaffRankMenuGui extends AlphaGuiScreen {
    private static final int CATEGORY_BASE = 40;
    private static final int BHOP = 80;
    private static final int BACK = 90;
    private static final int ACCENT = 0xFFFFD866;
    private final GuiScreen parent;
    private final List<String> categories = new ArrayList<String>();
    private String selected = "General";
    private int left, top, panelWidth, panelHeight;

    public StaffRankMenuGui(GuiScreen parent) { this.parent = parent; }

    private void buildCategories() {
        categories.clear();
        if (PermissionService.hasModAccess("staff.bhop")) categories.add("General");
        String[][] ranks = {
            {"T-Mod", "staff.rank.tmod"}, {"Mod", "staff.rank.mod"},
            {"Sr Mod", "staff.rank.srmod"}, {"Admin", "staff.rank.admin"},
            {"Sr Admin", "staff.rank.sradmin"}, {"Management", "staff.rank.management"},
            {"Owner", "staff.rank.owner"}, {"Founder", "staff.rank.founder"}
        };
        for (String[] rank : ranks) {
            if (PermissionService.hasModAccess(rank[1])) categories.add(rank[0]);
        }
        if (!categories.contains(selected)) selected = categories.isEmpty() ? "" : categories.get(0);
    }

    @Override public void initGui() {
        buttonList.clear();
        buildCategories();
        panelWidth = Math.min(620, width - 28);
        panelHeight = Math.min(360, height - 30);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        int x = left + 16;
        int y = top + 58;
        int catWidth = Math.min(170, panelWidth / 3);
        for (int i = 0; i < categories.size(); i++) {
            buttonList.add(new AlphaButton(CATEGORY_BASE + i, x, y + i * 27, catWidth, 22, categories.get(i)));
        }
        if ("General".equals(selected) && PermissionService.hasModAccess("staff.bhop")) {
            buttonList.add(new AlphaButton(BHOP, left + catWidth + 34, top + 68, 190, 34, "BHOP  /  SETTINGS"));
        }
        buttonList.add(new AlphaButton(BACK, left + 16, top + panelHeight - 32, 78, 20, "BACK"));
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x28000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xB8101218);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);
        drawString(fontRendererObj, "ALPHA", left + 16, top + 15, ACCENT);
        drawString(fontRendererObj, "STAFF MODS", left + 16, top + 30, 0xFFF4F5F8);
        drawString(fontRendererObj, "RANK CATEGORIES", left + 16, top + 46, 0xFFB8BDC8);
        if (categories.isEmpty()) {
            drawString(fontRendererObj, "No staff permissions are verified for this account.", left + 210, top + 72, 0xFFB8BDC8);
        } else if ("General".equals(selected)) {
            drawString(fontRendererObj, "GENERAL", left + 220, top + 48, ACCENT);
            drawString(fontRendererObj, "General staff tools available to your account.", left + 220, top + 64, 0xFFB8BDC8);
            drawString(fontRendererObj, "Choose a mod to open its settings.", left + 220, top + 81, 0xFFB8BDC8);
        } else {
            drawString(fontRendererObj, selected.toUpperCase(), left + 220, top + 48, ACCENT);
            drawString(fontRendererObj, "No tools are configured for this rank yet.", left + 220, top + 68, 0xFFB8BDC8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (button.id == BHOP && "General".equals(selected) && PermissionService.hasModAccess("staff.bhop")) {
            mc.displayGuiScreen(new StaffModsMenuGui(this)); return;
        }
        int index = button.id - CATEGORY_BASE;
        if (index >= 0 && index < categories.size()) {
            selected = categories.get(index);
            initGui();
        }
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}