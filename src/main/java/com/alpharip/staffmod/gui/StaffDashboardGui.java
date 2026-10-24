package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import java.io.IOException;

/**
 * Dedicated Staff Dashboard for shared staff records and module overviews;
 * shared live records must be supplied by the authenticated community data adapter.
 */
public final class StaffDashboardGui extends AlphaGuiScreen {
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
    private JsonArray records = new JsonArray();
    private String dataStatus = "Sign in to load shared staff data.";
    private long lastRefreshAt = 0L;

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

    public StaffDashboardGui(final net.minecraft.client.gui.GuiScreen parentScreen) {
        this(parentScreen, "dashboard");
    }

    public StaffDashboardGui(final net.minecraft.client.gui.GuiScreen parentScreen, String initialSection) {
        if ("cases".equalsIgnoreCase(initialSection)) section = Section.CASES;
        else if ("shifts".equalsIgnoreCase(initialSection)) section = Section.SHIFTS;
        else if ("presence".equalsIgnoreCase(initialSection)) section = Section.PRESENCE;
        else if ("audit".equalsIgnoreCase(initialSection)) section = Section.AUDIT;
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
        refreshRecords(false);

        int gap = 8;
        int columns = 2;
        int tileWidth = (contentRight - contentLeft - gap) / columns;
        int tileHeight = 34;

        if (section == Section.DASHBOARD) {
            buttonList.add(new AlphaButton(CASES, contentLeft, contentTop, tileWidth, tileHeight, "CASES"));
            buttonList.add(new AlphaButton(SHIFTS, contentLeft + tileWidth + gap, contentTop, tileWidth, tileHeight, "SHIFTS"));
            buttonList.add(new AlphaButton(PRESENCE, contentLeft, contentTop + tileHeight + gap, tileWidth, tileHeight, "PRESENCE"));
            buttonList.add(new AlphaButton(AUDIT, contentLeft + tileWidth + gap, contentTop + tileHeight + gap, tileWidth, tileHeight, "AUDIT"));
            buttonList.add(new AlphaButton(REFRESH, contentLeft, contentBottom - 27, tileWidth, 22, "REFRESH DASHBOARD"));
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
            refreshRecords(true);
        } else return;
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (System.currentTimeMillis() - lastRefreshAt >= 10000L) refreshRecords(false);
        drawRect(0, 0, width, height, 0x30000000);
        int right = panelLeft + panelWidth;
        int bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xE00B0D12);
        drawRect(panelLeft, panelTop, right, panelTop + 1, ACCENT);
        drawRect(panelLeft, panelTop + 1, panelLeft + 1, bottom - 1, 0x554B515D);
        drawRect(right - 1, panelTop + 1, right, bottom - 1, 0x554B515D);

        // Match Alpha Developer Studio's hierarchy and restrained workspace styling,
        // while retaining Staff Operations' existing panel dimensions and controls.
        drawString(fontRendererObj, "ALPHA  /  STAFF DASHBOARD", panelLeft + 16, panelTop + 13, 0xFFF0F1F4);
        drawString(fontRendererObj, "STAFF OVERVIEW WORKSPACE", panelLeft + 16, panelTop + 30, ACCENT);
        drawString(fontRendererObj, section.title, panelLeft + 16, panelTop + 49, 0xFFB8BDC8);

        int dividerY = panelTop + 66;
        drawRect(panelLeft + 14, dividerY, right - 14, dividerY + 1, 0x554B515D);

        if (section == Section.DASHBOARD) {
            drawDashboardSummary();
        } else {
            drawSectionDetails();
        }

        drawRect(panelLeft + 14, bottom - 36, right - 14, bottom - 35, 0x334B515D);
        drawString(fontRendererObj, "STATUS  " + dataStatus, panelLeft + 14, bottom - 25, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDashboardSummary() {
        int y = contentTop + 2 * (34 + 8) + 12;
        drawString(fontRendererObj, "TEAM OVERVIEW", contentLeft, y, 0xFFE6E8ED);
        drawString(fontRendererObj, "Cases, shifts, presence and audit activity", contentLeft, y + 14, 0xFFB8BDC8);
        drawString(fontRendererObj, "SHARED RECORDS: " + records.size(), contentLeft, y + 34, 0xFFFFD866);
        drawString(fontRendererObj, dataStatus, contentLeft, y + 49, 0xFFB8BDC8);
        drawRecordCounts(contentLeft, y + 66);
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
        drawString(fontRendererObj, "SHARED RECORDS: " + countRecords(section), contentLeft, y + 42, 0xFFFFD866);
        drawString(fontRendererObj, dataStatus, contentLeft, y + 57, 0xFFB8BDC8);
        drawRecentRecords(contentLeft, y + 75, section);
    }

    private void refreshRecords(boolean showStatus) {
        if (!SupabaseCommunityService.isSignedIn()) {
            records = new JsonArray();
            dataStatus = "Sign in to your Alpha Account to load live shared data.";
            return;
        }
        try {
            records = SupabaseCommunityService.getStaffOperationsRecords();
            dataStatus = "Synced with the shared Alpha staff database.";
            lastRefreshAt = System.currentTimeMillis();
        } catch (Exception exception) {
            dataStatus = "Shared data unavailable: " + safeMessage(exception.getMessage());
            if (showStatus) System.err.println("[AlphaClient] Staff Operations refresh failed: " + dataStatus);
        }
    }

    private String safeMessage(String message) {
        if (message == null || message.trim().isEmpty()) return "request failed";
        String clean = message.replace('\n', ' ').replace('\r', ' ');
        return clean.length() > 72 ? clean.substring(0, 72) : clean;
    }

    private int countRecords(Section target) {
        String type = target == Section.CASES ? "case" : target == Section.SHIFTS ? "shift"
                : target == Section.PRESENCE ? "presence" : target == Section.AUDIT ? "audit" : "";
        int count = 0;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).isJsonObject() && type.equals(records.get(i).getAsJsonObject().get("record_type").getAsString())) count++;
        }
        return count;
    }

    private void drawRecordCounts(int x, int y) {
        drawString(fontRendererObj, "Cases: " + countRecords(Section.CASES), x, y, 0xFFB8BDC8);
        drawString(fontRendererObj, "Shifts: " + countRecords(Section.SHIFTS), x + 120, y, 0xFFB8BDC8);
        drawString(fontRendererObj, "Presence: " + countRecords(Section.PRESENCE), x + 240, y, 0xFFB8BDC8);
        drawString(fontRendererObj, "Audit: " + countRecords(Section.AUDIT), x + 360, y, 0xFFB8BDC8);
    }

    private void drawRecentRecords(int x, int y, Section target) {
        String type = target == Section.CASES ? "case" : target == Section.SHIFTS ? "shift"
                : target == Section.PRESENCE ? "presence" : "audit";
        int shown = 0;
        for (int i = 0; i < records.size() && shown < 5; i++) {
            if (!records.get(i).isJsonObject()) continue;
            JsonObject record = records.get(i).getAsJsonObject();
            if (!type.equals(record.has("record_type") ? record.get("record_type").getAsString() : "")) continue;
            String title = record.has("title") ? record.get("title").getAsString() : "(untitled)";
            String status = record.has("status") ? record.get("status").getAsString() : "";
            String line = title + " [" + status + "]";
            if (line.length() > 68) line = line.substring(0, 65) + "...";
            drawString(fontRendererObj, line, x, y + shown * 13, 0xFFE6E8ED);
            shown++;
        }
        if (shown == 0) drawString(fontRendererObj, "No shared " + type + " records yet.", x, y, 0xFFB8BDC8);
    }

}
