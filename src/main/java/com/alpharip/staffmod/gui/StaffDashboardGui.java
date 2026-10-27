// --- Made By MsAlexTsuki ---
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
    private static final int CMD_WORKSPACE = 17;
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
    private final List<String> commandHistory = new ArrayList<String>();
    private int commandHistoryIndex = 0;
    private String commandStatus = "Enter help to list dashboard commands.";

    private enum Section {
        DASHBOARD("Dashboard"),
        CASES("Cases"),
        SHIFTS("Shifts"),
        PRESENCE("Presence"),
        AUDIT("Activity and audit"),
        CMD("CMD Workspace");

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
        else if ("cmd".equalsIgnoreCase(initialSection)) section = Section.CMD;
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
        panelWidth = Math.min(540, Math.max(340, width - 32));
        panelHeight = Math.min(330, Math.max(245, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        int listX = panelLeft + 12;
        int listWidth = Math.max(92, Math.min(104, panelWidth / 3));
        dividerX = listX + listWidth + 9;
        contentLeft = dividerX + 12;
        contentRight = panelLeft + panelWidth - 12;
        contentTop = panelTop + 58;
        contentBottom = panelTop + panelHeight - 54;
        refreshRecords(false);
        commandField = new GuiTextField(30, fontRendererObj, contentLeft + 42, panelTop + panelHeight - 72, Math.max(72, contentRight - contentLeft - 110), 15);
        commandField.setMaxStringLength(180);
        commandField.setCanLoseFocus(true);
        commandField.setFocused(false);

        buttonList.add(new AlphaButton(NAV_BASE, listX, panelTop + 50, listWidth, 17, "Overview", section == Section.DASHBOARD));
        buttonList.add(new AlphaButton(NAV_BASE + 1, listX, panelTop + 70, listWidth, 17, "Cases", section == Section.CASES));
        buttonList.add(new AlphaButton(NAV_BASE + 2, listX, panelTop + 90, listWidth, 17, "Shifts", section == Section.SHIFTS));
        buttonList.add(new AlphaButton(NAV_BASE + 3, listX, panelTop + 110, listWidth, 17, "Presence", section == Section.PRESENCE));
        buttonList.add(new AlphaButton(NAV_BASE + 4, listX, panelTop + 130, listWidth, 17, "Activity and audit", section == Section.AUDIT));
        buttonList.add(new AlphaButton(NAV_BASE + 5, listX, panelTop + 150, listWidth, 17, "CMD Workspace", section == Section.CMD));
        buttonList.add(new AlphaButton(REFRESH, panelLeft + panelWidth - 80, panelTop + 8, 68, 18, "Reload"));
        buttonList.add(new AlphaButton(RUN_COMMAND, contentRight - 64, panelTop + panelHeight - 72, 56, 15, "Run"));
        buttonList.get(buttonList.size() - 1).enabled = section == Section.CMD;
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 82, panelTop + panelHeight - 25, 70, 17, "Back"));
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
        else if (button.id == NAV_BASE + 5) section = Section.CMD;
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
        drawString(fontRendererObj, "ALPHA  /  STAFF WORKSPACE", panelLeft + 12, panelTop + 11, 0xFFF0F1F4);
        drawString(fontRendererObj, "STAFF OPERATIONS", panelLeft + 12, panelTop + 28, ACCENT);

        drawRect(dividerX, panelTop + 42, dividerX + 1, bottom - 62, 0x554B515D);
        drawString(fontRendererObj, section.title, contentLeft, panelTop + 43, ACCENT);

        if (section == Section.DASHBOARD) drawDashboardSummary();
        else if (section == Section.CMD) drawCommandWorkspace();
        else drawSectionDetails();

        drawRect(panelLeft + 14, bottom - 36, right - 14, bottom - 35, 0x334B515D);
        drawString(fontRendererObj, "STATUS  " + dataStatus, panelLeft + 14, bottom - 25, 0xFF9CE2AC);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawCommandWorkspace() {
        int bottom = panelTop + panelHeight;
        drawString(fontRendererObj, "Alpha", contentLeft, contentTop + 2, ACCENT);
        int consoleTop = contentTop + 20;
        int consoleBottom = bottom - 104;
        drawRect(contentLeft - 4, consoleTop, contentRight, consoleBottom, 0xAA050609);
        int maxLines = Math.max(1, (consoleBottom - consoleTop - 12) / 11);
        int start = Math.max(0, commandResults.size() - maxLines);
        int y = consoleTop + 8;
        for (int i = start; i < commandResults.size(); i++) {
            String line = commandResults.get(i);
            int maxChars = Math.max(20, (contentRight - contentLeft - 10) / 6);
            if (line.length() > maxChars) line = line.substring(0, maxChars - 3) + "...";
            drawString(fontRendererObj, "Alpha> " + line, contentLeft + 4, y, 0xFFE6E8ED);
            y += 11;
        }
        drawString(fontRendererObj, commandStatus, contentLeft, bottom - 91, 0xFF9CE2AC);
        if (commandField != null) {
            drawString(fontRendererObj, "COMMAND  (Enter to run, ↑/↓ history)", contentLeft, commandField.yPosition - 12, 0xFFB8BDC8);
            commandField.drawTextBox();
            drawString(fontRendererObj, "Alpha>", contentLeft, commandField.yPosition + 4, ACCENT);
        }
    }

    private void drawDashboardSummary() {
        int y = contentTop + 4;
        drawString(fontRendererObj, "Team overview", contentLeft, y, 0xFFE6E8ED);
        drawString(fontRendererObj, "Shared staff records and recent activity", contentLeft, y + 13, 0xFFB8BDC8);
        drawString(fontRendererObj, "RECORDS  " + records.size(), contentLeft, y + 30, 0xFFFFD866);
        drawString(fontRendererObj, dataStatus, contentLeft, y + 43, 0xFFB8BDC8);
        drawRecordCounts(contentLeft, y + 58);
        int activityY = y + 78;
        drawString(fontRendererObj, "RECENT ACTIVITY", contentLeft, activityY, 0xFFE6E8ED);
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
            drawString(fontRendererObj, line, contentLeft, activityY + 13 + shown * 11, 0xFFB8BDC8);
            shown++;
        }
        if (shown == 0) drawString(fontRendererObj, "No module activity has been recorded yet.", contentLeft, activityY + 14, 0xFFB8BDC8);
    }

    private void drawSectionDetails() {
        int y = contentTop + 2;
        drawString(fontRendererObj, section.title + " OVERVIEW", contentLeft, y, 0xFFE6E8ED);
        y += 16;
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
        drawString(fontRendererObj, "RECORDS  " + countRecords(section), contentLeft, y + 36, 0xFFFFD866);
        drawString(fontRendererObj, dataStatus, contentLeft, y + 49, 0xFFB8BDC8);
        drawRecentRecords(contentLeft, y + 64, section);
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
        drawString(fontRendererObj, "Cases " + countRecords(Section.CASES), x, y, 0xFFB8BDC8);
        drawString(fontRendererObj, "Shifts " + countRecords(Section.SHIFTS), x + 82, y, 0xFFB8BDC8);
        drawString(fontRendererObj, "Presence " + countRecords(Section.PRESENCE), x + 164, y, 0xFFB8BDC8);
        drawString(fontRendererObj, "Audit " + countRecords(Section.AUDIT), x + 258, y, 0xFFB8BDC8);
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
                if (line.length() > 66) line = line.substring(0, 63) + "...";
                drawString(fontRendererObj, line, x, y + shown * 16, 0xFFE6E8ED);
                if (!when.isEmpty()) {
                    String time = when.replace('T', ' ');
                    if (time.length() > 19) time = time.substring(0, 19);
                    drawString(fontRendererObj, time, x + 8, y + shown * 16 + 9, 0xFF9299A6);
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

    private void runSuspectCommand(String[] parts, String input) {
        if (!SupabaseCommunityService.isSignedIn()) {
            commandStatus = "Sign in to your Alpha Account to use shared Investigating data.";
            return;
        }
        String sub = parts.length > 1 ? parts[1].trim() : "";
        boolean check = "check".equalsIgnoreCase(sub);
        boolean list = "list".equalsIgnoreCase(sub);
        String player = check ? (parts.length > 2 ? parts[2].trim() : "")
                : list ? "" : (parts.length > 1 ? input.substring(input.indexOf(parts[1])).trim() : "");
        if (check && player.isEmpty()) { commandStatus = "Usage: ./suspect check <player>"; return; }
        if (!check && !list && (player.isEmpty() || player.contains(" "))) {
            commandStatus = "Usage: ./suspect <player> | ./suspect check <player> | ./suspect list";
            return;
        }
        try {
            JsonArray all = SupabaseCommunityService.getStaffOperationsRecords();
            if (list && !canReviewAllSuspects()) {
                commandStatus = "The suspect list is restricted to Manager, Owner and Founder.";
                return;
            }
            int shown = 0;
            for (int i = 0; i < all.size(); i++) {
                if (!all.get(i).isJsonObject()) continue;
                JsonObject row = all.get(i).getAsJsonObject();
                if (!"suspect".equals(value(row, "record_type", ""))) continue;
                String markedPlayer = value(row, "title", "");
                if (check && !markedPlayer.equalsIgnoreCase(player)) continue;
                if (!check && !list && !markedPlayer.equalsIgnoreCase(player)) continue;
                if (list || check) {
                    commandResults.add(markedPlayer + " [SUSPECT] / marked by "
                            + value(row, "created_by_label", value(row, "created_by", "staff")));
                    shown++;
                } else {
                    SupabaseCommunityService.deleteStaffOperationsRecord(value(row, "id", ""));
                    commandStatus = "Removed suspect mark from " + player + ".";
                    refreshRecords(false);
                    return;
                }
            }
            if (check) commandStatus = shown == 0 ? player + " is not marked as suspect." : player + " is marked as suspect.";
            else if (list) commandStatus = "Showing " + shown + " suspect record(s).";
            else {
                JsonObject payload = new JsonObject();
                payload.addProperty("player_name", player);
                payload.addProperty("created_by_label", SupabaseCommunityService.getSignedInAlphaName());
                payload.addProperty("details", "Marked for staff attention; not proof of cheating.");
                SupabaseCommunityService.createStaffOperationsRecord("suspect", player, "open", payload);
                SupabaseCommunityService.recordStaffModuleActivity("staff_operations", "suspect_marked", "suspect_added", player, payload);
                commandResults.add(player + " added to Investigating.");
                commandStatus = "Suspect mark saved to shared staff database.";
                refreshRecords(false);
            }
        } catch (Exception e) {
            commandStatus = "Investigating command failed: " + safeMessage(e.getMessage());
        }
    }

    private boolean canReviewAllSuspects() {
        if (!PermissionService.isOnlineRankVerified()) return false;
        java.util.Set<PermissionService.Rank> ranks = PermissionService.getVerifiedOnlineRanks();
        return ranks != null && (ranks.contains(PermissionService.Rank.MANAGER)
                || ranks.contains(PermissionService.Rank.OWNER)
                || ranks.contains(PermissionService.Rank.FOUNDER));
    }

    private void runDashboardCommand(String raw) {
        String input = raw == null ? "" : raw.trim();
        if (input.isEmpty()) { commandStatus = "Enter a command. Use help for the list."; return; }
        commandHistory.add(input);
        commandHistoryIndex = commandHistory.size();
        commandResults.add("> " + input);
        if (commandResults.size() > 200) commandResults.remove(0);
        String[] parts = input.split("\\s+", 3);
        String command = parts[0].toLowerCase(java.util.Locale.ROOT);
        if (command.startsWith("./")) command = command.substring(2);
        else if (command.startsWith("/")) command = command.substring(1);
        if ("help".equals(command)) {
            commandResults.add("help | cases | shifts | presence [player]");
            commandResults.add("afk [note] | busy [note] | available [note]");
            commandResults.add("/suspect <player> | /suspect check <player> | /suspect list");
            commandResults.add("/investigating <note> | /investigating delete | activity [player]");
            commandResults.add("loginlog | notes <player>");
            commandResults.add("addnote <player> <note>");
            commandStatus = "Status commands save shared Presence timeline records.";
        } else if ("cases".equals(command) || "shifts".equals(command) || "presence".equals(command)) {
            String type = "cases".equals(command) ? "case" : "shifts".equals(command) ? "shift" : "presence";
            int shown = 0;
            for (int i = 0; i < records.size() && shown < 4; i++) {
                if (!records.get(i).isJsonObject()) continue;
                JsonObject row = records.get(i).getAsJsonObject();
                if (!type.equals(value(row, "record_type", ""))) continue;
                commandResults.add(value(row, "title", "(untitled)") + " [" + value(row, "status", "unknown") + "]");
                shown++;
            }
            commandStatus = shown == 0 ? "No " + command + " records found." : "Showing " + shown + " " + command + " record(s).";
        } else if ("suspect".equals(command)) {
            runSuspectCommand(parts, input);
        } else if ("activity".equals(command)) {
            String filter = parts.length > 1 ? parts[1].toLowerCase(java.util.Locale.ROOT) : "";
            int shown = 0;
            for (int i = 0; i < moduleActivity.size() && shown < 2; i++) {
                if (!moduleActivity.get(i).isJsonObject()) continue;
                JsonObject event = moduleActivity.get(i).getAsJsonObject();
                String actor = value(event, "actor_label", value(event, "actor_id", "Unknown staff"));
                if (!filter.isEmpty() && !actor.toLowerCase(java.util.Locale.ROOT).contains(filter)) continue;
                String line = actor + " / " + value(event, "module_key", "module") + " / " + value(event, "action_key", "activity");
                commandResults.add(line.length() > 78 ? line.substring(0, 75) + "..." : line);
                shown++;
            }
            commandStatus = shown == 0 ? "No matching module activity found." : "Latest matching module activity";
        } else if ("investigating".equals(command) && parts.length > 1 && "delete".equalsIgnoreCase(parts[1])) {
            if (!SupabaseCommunityService.isSignedIn()) {
                commandStatus = "Sign in to your Alpha Account to remove your test entry.";
            } else {
                try {
                    String alphaName = SupabaseCommunityService.getSignedInAlphaName();
                    JsonArray shared = SupabaseCommunityService.getStaffOperationsRecords();
                    int removed = 0;
                    for (int i = 0; i < shared.size(); i++) {
                        if (!shared.get(i).isJsonObject()) continue;
                        JsonObject row = shared.get(i).getAsJsonObject();
                        if (!"presence".equals(value(row, "record_type", ""))) continue;
                        JsonObject payload = row.has("payload") && row.get("payload").isJsonObject()
                                ? row.getAsJsonObject("payload") : null;
                        if (!"investigating".equalsIgnoreCase(value(payload, "presence_status", ""))) continue;
                        if (!alphaName.equalsIgnoreCase(value(payload, "actor_label", ""))) continue;
                        SupabaseCommunityService.deleteStaffOperationsRecord(value(row, "id", ""));
                        removed++;
                    }
                    refreshRecords(true);
                    commandStatus = removed == 0
                            ? "No legacy Investigating presence entry found for your Alpha account."
                            : "Removed " + removed + " legacy Investigating presence entr" + (removed == 1 ? "y." : "ies.");
                    commandResults.add(commandStatus);
                } catch (Exception e) {
                    commandStatus = "Could not remove legacy Investigating entry: " + safeMessage(e.getMessage());
                }
            }
        } else if ("afk".equals(command) || "busy".equals(command) || "available".equals(command)) {
            String presenceStatus = "afk".equals(command) ? "away" : "busy".equals(command) ? "busy" : "active";
            String note = input.length() > parts[0].length() ? input.substring(parts[0].length()).trim() : "";
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("details", note);
                payload.addProperty("presence_status", command);
                payload.addProperty("event", "status_changed");
                payload.addProperty("actor_label", SupabaseCommunityService.getSignedInAlphaName());
                SupabaseCommunityService.createStaffOperationsRecord("presence",
                        SupabaseCommunityService.getSignedInAlphaName() + " - " + command,
                        presenceStatus, payload);
                SupabaseCommunityService.recordStaffModuleActivity("staff_presence", "action_performed",
                        "presence_status_" + command, SupabaseCommunityService.getSignedInAlphaName(), payload);
                refreshRecords(true);
                commandStatus = "Presence status saved to shared staff records.";
                commandResults.add(command + (note.isEmpty() ? "" : ": " + note));
            } catch (Exception e) {
                commandStatus = "Could not save presence status: " + safeMessage(e.getMessage());
            }
        } else if ("loginlog".equals(command)) {
            refreshRecords(false);
            int shown = 0;
            for (int i = 0; i < moduleActivity.size() && shown < 2; i++) {
                if (!moduleActivity.get(i).isJsonObject()) continue;
                JsonObject event = moduleActivity.get(i).getAsJsonObject();
                String action = value(event, "action_key", "");
                if (!"session_login".equals(action) && !"session_logout".equals(action)) continue;
                String line = value(event, "actor_label", value(event, "actor_id", "Unknown staff"))
                        + " / " + ("session_login".equals(action) ? "Login" : "Logout")
                        + " / " + value(event, "occurred_at", "");
                commandResults.add(line.length() > 78 ? line.substring(0, 75) + "..." : line);
                shown++;
            }
            commandStatus = shown == 0
                    ? "No login/logout events found in the readable staff activity records."
                    : "Latest recorded staff login/logout events.";
        } else if ("notes".equals(command)) {
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                commandStatus = "Usage: notes <player>";
            } else {
                try {
                    JsonArray notes = SupabaseCommunityService.getStaffPlayerNotes(parts[1].trim());
                    int shown = 0;
                    for (int i = 0; i < notes.size() && shown < 2; i++) {
                        if (!notes.get(i).isJsonObject()) continue;
                        JsonObject note = notes.get(i).getAsJsonObject();
                        String line = value(note, "player_name", parts[1]) + " / "
                                + value(note, "created_by_label", "Staff") + " / "
                                + value(note, "note", "");
                        commandResults.add(line.length() > 78 ? line.substring(0, 75) + "..." : line);
                        shown++;
                    }
                    commandStatus = shown == 0 ? "No staff notes found for " + parts[1] + "." : "Found " + shown + " staff note(s).";
                } catch (Exception e) {
                    commandStatus = "Notes lookup failed: " + safeMessage(e.getMessage());
                }
            }
        } else if ("addnote".equals(command)) {
            if (parts.length < 3 || parts[1].trim().isEmpty() || parts[2].trim().isEmpty()) {
                commandStatus = "Usage: addnote <player> <note>";
            } else {
                try {
                    JsonObject created = SupabaseCommunityService.createStaffPlayerNote(parts[1].trim(), parts[2].trim());
                    commandResults.add(value(created, "player_name", parts[1]) + " / " + value(created, "note", parts[2]));
                    commandStatus = "Staff note saved to the shared database.";
                    refreshRecords(false);
                } catch (Exception e) {
                    commandStatus = "Could not save staff note: " + safeMessage(e.getMessage());
                }
            }
        } else { commandStatus = "Unknown command: " + parts[0] + ". Enter help."; }
        if (commandField != null) {
            commandField.setText("");
            commandField.setFocused(true);
        }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) { parent.returnToStaffMods(); return; }
        if (section != Section.CMD) { super.keyTyped(typedChar, keyCode); return; }
        if (commandField != null && commandField.isFocused()) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) { runDashboardCommand(commandField.getText()); return; }
            if (keyCode == Keyboard.KEY_UP) {
                if (!commandHistory.isEmpty()) {
                    commandHistoryIndex = Math.max(0, commandHistoryIndex - 1);
                    commandField.setText(commandHistory.get(commandHistoryIndex));
                }
                return;
            }
            if (keyCode == Keyboard.KEY_DOWN) {
                if (!commandHistory.isEmpty()) {
                    commandHistoryIndex = Math.min(commandHistory.size(), commandHistoryIndex + 1);
                    commandField.setText(commandHistoryIndex >= commandHistory.size() ? "" : commandHistory.get(commandHistoryIndex));
                }
                return;
            }
            commandField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (commandField != null) {
            commandField.mouseClicked(mouseX, mouseY, mouseButton);
            boolean inside = mouseX >= commandField.xPosition && mouseX < commandField.xPosition + commandField.width
                    && mouseY >= commandField.yPosition && mouseY < commandField.yPosition + commandField.height;
            commandField.setFocused(section == Section.CMD && inside && mouseButton == 0);
        }
    }
    private String value(JsonObject object, String key, String fallback) {
        try {
            return object != null && object.has(key) && !object.get(key).isJsonNull()
                    ? object.get(key).getAsString() : fallback;
        } catch (Exception ignored) { return fallback; }
    }

}
