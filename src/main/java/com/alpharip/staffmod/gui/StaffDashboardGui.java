package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private static final int RUN_COMMAND = 16;
    private static final int ACCENT = 0xFFFFD866;
    private static final int NAV_BASE = 100;
    private int dividerX;

    private final GuiScreenParent parent;
    private Section section = Section.DASHBOARD;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int contentLeft, contentTop, contentRight, contentBottom;
    private JsonArray records = new JsonArray();
    private JsonArray auditLogs = new JsonArray();
    private JsonArray moduleActivity = new JsonArray();
    private String dataStatus = "Sign in to load shared staff data.";
    private long lastRefreshAt = 0L;
    private GuiTextField commandField;
    private final List<String> commandResults = new ArrayList<String>();
    private String commandStatus = "Enter help to list dashboard commands.";

    private enum Section {
        DASHBOARD("Dashboard"),
        CASES("Cases"),
        SHIFTS("Shifts"),
        PRESENCE("Presence"),
        AUDIT("Activity and audit");

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

        // Preserve the dashboard GUI dimensions from the original implementation.
        panelWidth = Math.min(590, Math.max(360, width - 32));
        panelHeight = Math.min(350, Math.max(250, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        int listX = panelLeft + 14;
        int listWidth = Math.max(112, Math.min(132, panelWidth / 3));
        dividerX = listX + listWidth + 12;
        contentLeft = dividerX + 18;
        contentRight = panelLeft + panelWidth - 16;
        contentTop = panelTop + 72;
        contentBottom = panelTop + panelHeight - 76;
        refreshRecords(false);
        commandField = new GuiTextField(30, fontRendererObj, contentLeft, panelTop + panelHeight - 83, Math.max(90, contentRight - contentLeft - 62), 20);
        commandField.setMaxStringLength(180);
        commandField.setCanLoseFocus(false);

        buttonList.add(new AlphaButton(NAV_BASE, listX, panelTop + 62, listWidth, 20, "Overview", section == Section.DASHBOARD));
        buttonList.add(new AlphaButton(NAV_BASE + 1, listX, panelTop + 85, listWidth, 20, "Cases", section == Section.CASES));
        buttonList.add(new AlphaButton(NAV_BASE + 2, listX, panelTop + 108, listWidth, 20, "Shifts", section == Section.SHIFTS));
        buttonList.add(new AlphaButton(NAV_BASE + 3, listX, panelTop + 131, listWidth, 20, "Presence", section == Section.PRESENCE));
        buttonList.add(new AlphaButton(NAV_BASE + 4, listX, panelTop + 154, listWidth, 20, "Activity and audit", section == Section.AUDIT));
        int actionWidth = Math.max(76, (contentRight - contentLeft - 8) / 2);
        buttonList.add(new AlphaButton(REFRESH, contentLeft, panelTop + panelHeight - 58, actionWidth, 20, "Refresh"));
        buttonList.add(new AlphaButton(RUN_COMMAND, contentRight - 54, panelTop + panelHeight - 83, 54, 20, "Run"));
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 94, panelTop + panelHeight - 31, 80, 20, "Back"));
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
        if (button.id == RUN_COMMAND) { runDashboardCommand(commandField == null ? "" : commandField.getText()); return; }
        if (button.id == NAV_BASE) section = Section.DASHBOARD;
        else if (button.id == NAV_BASE + 1) section = Section.CASES;
        else if (button.id == NAV_BASE + 2) section = Section.SHIFTS;
        else if (button.id == NAV_BASE + 3) section = Section.PRESENCE;
        else if (button.id == NAV_BASE + 4) section = Section.AUDIT;
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
        drawString(fontRendererObj, "ALPHA  /  STAFF INFORMATION", panelLeft + 16, panelTop + 13, 0xFFF0F1F4);
        drawString(fontRendererObj, "STAFF WORKSPACE  •  GENERAL", panelLeft + 16, panelTop + 30, ACCENT);
        drawString(fontRendererObj, "WORKSPACE", panelLeft + 16, panelTop + 49, 0xFFB8BDC8);

        drawRect(dividerX, panelTop + 48, dividerX + 1, bottom - 80, 0x554B515D);
        drawString(fontRendererObj, section.title, contentLeft, panelTop + 49, ACCENT);

        if (section == Section.DASHBOARD) {
            drawDashboardSummary();
        } else {
            drawSectionDetails();
        }

        drawRect(panelLeft + 14, bottom - 36, right - 14, bottom - 35, 0x334B515D);
        drawString(fontRendererObj, "STATUS  " + dataStatus, panelLeft + 14, bottom - 25, 0xFF9CE2AC);
        drawString(fontRendererObj, "Dashboard command", contentLeft, bottom - 100, 0xFFE6E8ED);
        if (commandField != null) commandField.drawTextBox();
        drawString(fontRendererObj, commandStatus, contentLeft, bottom - 61, 0xFFB8BDC8);
        for (int i = 0; i < commandResults.size() && i < 4; i++) drawString(fontRendererObj, commandResults.get(i), contentLeft, bottom - 47 + i * 10, 0xFFE6E8ED);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDashboardSummary() {
        int y = contentTop + 4;
        drawString(fontRendererObj, "Team overview", contentLeft, y, 0xFFE6E8ED);
        drawString(fontRendererObj, "Shared staff records and recorded activity", contentLeft, y + 14, 0xFFB8BDC8);
        drawString(fontRendererObj, "Shared records: " + records.size(), contentLeft, y + 34, 0xFFFFD866);
        drawString(fontRendererObj, dataStatus, contentLeft, y + 49, 0xFFB8BDC8);
        drawRecordCounts(contentLeft, y + 66);
        int activityY = y + 92;
        drawString(fontRendererObj, "Recent staff module activity", contentLeft, activityY, 0xFFE6E8ED);
        int shown = 0;
        for (int i = 0; i < moduleActivity.size() && shown < 5; i++) {
            if (!moduleActivity.get(i).isJsonObject()) continue;
            JsonObject event = moduleActivity.get(i).getAsJsonObject();
            String actor = value(event, "actor_label", value(event, "actor_id", "Unknown staff"));
            String module = value(event, "module_key", "module");
            String action = value(event, "action_key", value(event, "event_type", "activity"));
            String when = value(event, "occurred_at", "");
            String line = actor + " / " + module + " / " + action + " / " + when;
            if (line.length() > 88) line = line.substring(0, 85) + "...";
            drawString(fontRendererObj, line, contentLeft, activityY + 14 + shown * 12, 0xFFB8BDC8);
            shown++;
        }
        if (shown == 0) drawString(fontRendererObj, "No module activity has been recorded yet.", contentLeft, activityY + 14, 0xFFB8BDC8);
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
            auditLogs = new JsonArray();
            moduleActivity = new JsonArray();
            dataStatus = "Sign in to your Alpha Account to load live shared data.";
            return;
        }
        try {
            records = SupabaseCommunityService.getStaffOperationsRecords();
            try { auditLogs = SupabaseCommunityService.getStaffAuditLogs(); }
            catch (Exception auditException) { auditLogs = new JsonArray(); }
            try { moduleActivity = SupabaseCommunityService.getStaffModuleActivity(); }
            catch (Exception activityException) { moduleActivity = new JsonArray(); }
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
        if (target == Section.AUDIT) {
            int shown = 0;
            for (int i = 0; i < auditLogs.size() && shown < 7; i++) {
                if (!auditLogs.get(i).isJsonObject()) continue;
                JsonObject event = auditLogs.get(i).getAsJsonObject();
                String actor = value(event, "actor_label", value(event, "actor_id", "Unknown staff"));
                String action = value(event, "action", "Recorded event");
                String category = value(event, "category", value(event, "product", "client"));
                String outcome = value(event, "outcome", "success");
                String when = value(event, "occurred_at", "");
                String line = actor + "  /  " + category + "  /  " + action + "  /  " + outcome;
                if (line.length() > 84) line = line.substring(0, 81) + "...";
                drawString(fontRendererObj, line, x, y + shown * 13, 0xFFE6E8ED);
                if (!when.isEmpty()) {
                    String time = when.replace('T', ' ');
                    if (time.length() > 19) time = time.substring(0, 19);
                    drawString(fontRendererObj, time, x + 8, y + shown * 13 + 9, 0xFF9299A6);
                }
                shown++;
            }
            if (shown == 0) drawString(fontRendererObj, "No readable audit events returned. Check account access and audit policies.", x, y, 0xFFB8BDC8);
            return;
        }
        String type = target == Section.CASES ? "case" : target == Section.SHIFTS ? "shift" : "presence";
        int shown = 0;
        for (int i = 0; i < records.size() && shown < 7; i++) {
            if (!records.get(i).isJsonObject()) continue;
            JsonObject record = records.get(i).getAsJsonObject();
            if (!type.equals(record.has("record_type") ? record.get("record_type").getAsString() : "")) continue;
            String title = value(record, "title", "(untitled)");
            String status = value(record, "status", "unknown");
            String actor = value(record, "created_by", "unknown staff");
            String assigned = value(record, "assigned_to", "unassigned");
            String line = title + " [" + status + "] by " + actor + " -> " + assigned;
            if (line.length() > 84) line = line.substring(0, 81) + "...";
            drawString(fontRendererObj, line, x, y + shown * 13, 0xFFE6E8ED);
            shown++;
        }
        if (shown == 0) drawString(fontRendererObj, "No shared " + type + " records yet.", x, y, 0xFFB8BDC8);
    }

    private String value(JsonObject object, String key, String fallback) {
        try {
            return object != null && object.has(key) && !object.get(key).isJsonNull()
                    ? object.get(key).getAsString() : fallback;
        } catch (Exception ignored) { return fallback; }
    }

}
