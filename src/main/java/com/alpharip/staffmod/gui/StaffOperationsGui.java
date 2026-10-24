package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import net.minecraft.client.gui.GuiButton;
import java.io.IOException;

/**
 * Staff Operations hub. The dashboard and its dedicated sections share one UI;
 * shared live records must be supplied by the authenticated community data adapter.
 */
public final class StaffOperationsGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int DASHBOARD = 10;
    private static final int CASES = 11;
    private static final int SHIFTS = 12;
    private static final int PRESENCE = 13;
    private static final int AUDIT = 14;
    private static final int REFRESH = 15;
    private static final int ACCENT = 0xFFFFD866;

    private final GuiScreenParent parent;
    private Section section = Section.DASHBOARD;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int contentLeft, contentTop, contentRight, contentBottom;

    private enum Section {
        DASHBOARD("STAFF DASHBOARD"),
        CASES("CASES"),
        SHIFTS("SHIFTS"),
        PRESENCE("PRESENCE"),
        AUDIT("AUDIT");

        private final String title;
        Section(String title) { this.title = title; }
    }

    /** Small wrapper keeps the existing screen as the return destination. */
    public interface GuiScreenParent {
        void returnToStaffMods();
    }

    public StaffOperationsGui(final net.minecraft.client.gui.GuiScreen parentScreen) {
        this.parent = new GuiScreenParent() {
            @Override public void returnToStaffMods() {
                net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(parentScreen);
            }
        };
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!PermissionService.hasStaffModsAccess()) {
            parent.returnToStaffMods();
            return;
        }

        panelWidth = Math.min(590, Math.max(360, width - 32));
        panelHeight = Math.min(350, Math.max(250, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        contentLeft = panelLeft + 16;
        contentRight = panelLeft + panelWidth - 16;
        contentTop = panelTop + 78;
        contentBottom = panelTop + panelHeight - 43;

        int gap = 8;
        int columns = 2;
        int tileWidth = (contentRight - contentLeft - gap) / columns;
        int tileHeight = 34;

        if (section == Section.DASHBOARD) {
            buttonList.add(new AlphaButton(CASES, contentLeft, contentTop, tileWidth, tileHeight, "CASES"));
            buttonList.add(new AlphaButton(SHIFTS, contentLeft + tileWidth + gap, contentTop, tileWidth, tileHeight, "SHIFTS"));
            buttonList.add(new AlphaButton(PRESENCE, contentLeft, contentTop + tileHeight + gap, tileWidth, tileHeight, "PRESENCE"));
            buttonList.add(new AlphaButton(AUDIT, contentLeft + tileWidth + gap, contentTop + tileHeight + gap, tileWidth, tileHeight, "AUDIT"));
            buttonList.add(new AlphaButton(REFRESH, contentLeft, contentBottom - 27, tileWidth, 22, "REFRESH OVERVIEW"));
        } else {
            buttonList.add(new AlphaButton(DASHBOARD, contentLeft, contentBottom - 27, tileWidth, 22, "DASHBOARD"));
            buttonList.add(new AlphaButton(REFRESH, contentLeft + tileWidth + gap, contentBottom - 27, tileWidth, 22, "REFRESH"));
        }
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 94, panelTop + panelHeight - 31, 80, 20, "BACK"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (!PermissionService.hasStaffModsAccess()) {
            parent.returnToStaffMods();
            return;
        }
        if (button.id == BACK) {
            parent.returnToStaffMods();
            return;
        }
        if (button.id == DASHBOARD) section = Section.DASHBOARD;
        else if (button.id == CASES) section = Section.CASES;
        else if (button.id == SHIFTS) section = Section.SHIFTS;
        else if (button.id == PRESENCE) section = Section.PRESENCE;
        else if (button.id == AUDIT) section = Section.AUDIT;
        else if (button.id == REFRESH) {
            // Rebuild the view from the current provider state when the live data
            // adapter is connected. Do not fabricate local team records.
        } else return;
        initGui();
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

        drawString(fontRendererObj, "ALPHA CLIENT", panelLeft + 14, panelTop + 14, 0xFFF0F1F4);
        drawString(fontRendererObj, "STAFF OPERATIONS", panelLeft + 14, panelTop + 34, ACCENT);
        drawString(fontRendererObj, section.title, contentLeft, panelTop + 57, 0xFFB8BDC8);

        if (section == Section.DASHBOARD) {
            drawDashboardSummary();
        } else {
            drawSectionDetails();
        }

        drawString(fontRendererObj, "RANK ACCESS: " + PermissionService.getEffectiveRank().name(),
                panelLeft + 14, bottom - 22, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDashboardSummary() {
        int y = contentTop + 2 * (34 + 8) + 12;
        drawString(fontRendererObj, "TEAM OVERVIEW", contentLeft, y, 0xFFE6E8ED);
        drawString(fontRendererObj, "Cases, shifts, presence and audit activity", contentLeft, y + 14, 0xFFB8BDC8);
        drawString(fontRendererObj, "LIVE DATA: NOT CONNECTED", contentLeft, y + 34, 0xFFFFD866);
        drawString(fontRendererObj, "Shared staff records will appear here when the authenticated", contentLeft, y + 49, 0xFFB8BDC8);
        drawString(fontRendererObj, "community data provider is configured.", contentLeft, y + 61, 0xFFB8BDC8);
    }

    private void drawSectionDetails() {
        int y = contentTop + 2;
        drawString(fontRendererObj, section.title + " OVERVIEW", contentLeft, y, 0xFFE6E8ED);
        y += 20;
        if (section == Section.CASES) {
            drawString(fontRendererObj, "Board, calendar, search, filters, templates,", contentLeft, y, 0xFFB8BDC8);
            drawString(fontRendererObj, "comments, checklists and assignments.", contentLeft, y + 13, 0xFFB8BDC8);
        } else if (section == Section.SHIFTS) {
            drawString(fontRendererObj, "Planner, tracker, notes, shift/session summaries", contentLeft, y, 0xFFB8BDC8);
            drawString(fontRendererObj, "and assignment history.", contentLeft, y + 13, 0xFFB8BDC8);
        } else if (section == Section.PRESENCE) {
            drawString(fontRendererObj, "Team availability, AFK, Busy, investigation status", contentLeft, y, 0xFFB8BDC8);
            drawString(fontRendererObj, "and the shared presence timeline.", contentLeft, y + 13, 0xFFB8BDC8);
        } else if (section == Section.AUDIT) {
            drawString(fontRendererObj, "Audit viewer, timeline, search, filters and export.", contentLeft, y, 0xFFB8BDC8);
        }
        drawString(fontRendererObj, "LIVE DATA: NOT CONNECTED", contentLeft, y + 42, 0xFFFFD866);
        drawString(fontRendererObj, "This section will use shared records, not local mock data.", contentLeft, y + 57, 0xFFB8BDC8);
    }
}
