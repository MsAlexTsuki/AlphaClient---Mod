package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Dedicated command workspace, separate from the Staff Dashboard. */
public final class CmdWorkspaceGui extends AlphaGuiScreen {
    private static final int BACK = 1;
    private static final int RUN = 2;
    private static final int CLEAR = 3;
    private final GuiScreen parent;
    private GuiTextField input;
    private final List<String> output = new ArrayList<String>();
    private String status = "Ready. Enter help to list commands.";
    private int panelLeft, panelTop, panelWidth, panelHeight;

    public CmdWorkspaceGui(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        if (!PermissionService.hasStaffModsAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        panelWidth = Math.min(620, Math.max(360, width - 32));
        panelHeight = Math.min(360, Math.max(250, height - 32));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        input = new GuiTextField(40, fontRendererObj, panelLeft + 16, panelTop + panelHeight - 54, panelWidth - 112, 22);
        input.setMaxStringLength(240);
        input.setCanLoseFocus(true);
        input.setFocused(true);
        buttonList.add(new AlphaButton(RUN, panelLeft + panelWidth - 88, panelTop + panelHeight - 54, 72, 22, "Run"));
        buttonList.add(new AlphaButton(CLEAR, panelLeft + panelWidth - 172, panelTop + panelHeight - 24, 72, 18, "Clear"));
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 88, panelTop + panelHeight - 24, 72, 18, "Back"));
        if (output.isEmpty()) {
            output.add("ALPHA CMD WORKSPACE");
            output.add("Type help to view available commands.");
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (!PermissionService.hasStaffModsAccess()) { mc.displayGuiScreen(parent); return; }
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (button.id == CLEAR) { output.clear(); status = "Output cleared."; return; }
        if (button.id == RUN) { execute(input == null ? "" : input.getText()); }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x50000000);
        int right = panelLeft + panelWidth, bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xEF0B0D12);
        drawRect(panelLeft, panelTop, right, panelTop + 1, 0xFFFFD866);
        drawString(fontRendererObj, "ALPHA  /  STAFF INFORMATION", panelLeft + 16, panelTop + 13, 0xFFF0F1F4);
        drawString(fontRendererObj, "CMD WORKSPACE", panelLeft + 16, panelTop + 31, 0xFFFFD866);
        drawString(fontRendererObj, "Standalone command console · Enter runs the command", panelLeft + 16, panelTop + 48, 0xFFB8BDC8);
        drawRect(panelLeft + 14, panelTop + 65, right - 14, bottom - 83, 0xAA050609);
        int maxLines = Math.max(1, (bottom - panelTop - 164) / 12);
        int start = Math.max(0, output.size() - maxLines);
        int y = panelTop + 76;
        for (int i = start; i < output.size(); i++) {
            String line = output.get(i);
            int maxChars = Math.max(20, (panelWidth - 36) / 6);
            if (line.length() > maxChars) line = line.substring(0, maxChars - 3) + "...";
            drawString(fontRendererObj, line, panelLeft + 22, y, 0xFFE6E8ED);
            y += 12;
        }
        drawString(fontRendererObj, status, panelLeft + 16, bottom - 72, 0xFF9CE2AC);
        if (input != null) input.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void execute(String raw) {
        String commandLine = raw == null ? "" : raw.trim();
        if (commandLine.isEmpty()) { status = "Enter a command."; return; }
        output.add("> " + commandLine);
        String[] args = commandLine.split("\\s+", 3);
        String command = args[0].toLowerCase(java.util.Locale.ROOT);
        try {
            if ("help".equals(command)) {
                output.add("help | cases | shifts | presence");
                output.add("afk [note] | busy [note] | available [note]");
                output.add("investigating <note> | activity [player] | loginlog");
                output.add("notes <player> | addnote <player> <note>");
                status = "Command reference.";
            } else if ("cases".equals(command) || "shifts".equals(command) || "presence".equals(command)) {
                JsonArray records = SupabaseCommunityService.getStaffOperationsRecords();
                String type = "cases".equals(command) ? "case" : "shifts".equals(command) ? "shift" : "presence";
                int count = 0;
                for (int i = 0; i < records.size() && count < 12; i++) {
                    if (!records.get(i).isJsonObject()) continue;
                    JsonObject row = records.get(i).getAsJsonObject();
                    if (!type.equals(value(row, "record_type", ""))) continue;
                    output.add(value(row, "title", "(untitled)") + " [" + value(row, "status", "unknown") + "]");
                    count++;
                }
                status = count == 0 ? "No " + command + " records found." : "Listed " + count + " record(s).";
            } else if ("afk".equals(command) || "busy".equals(command) || "available".equals(command) || "investigating".equals(command)) {
                String note = commandLine.length() > args[0].length() ? commandLine.substring(args[0].length()).trim() : "";
                if ("investigating".equals(command) && note.isEmpty()) { status = "Usage: investigating <note>"; return; }
                String presenceStatus = "afk".equals(command) ? "away" : "busy".equals(command) ? "busy" : "available".equals(command) ? "active" : "in_progress";
                JsonObject payload = new JsonObject();
                payload.addProperty("details", note);
                payload.addProperty("presence_status", command);
                payload.addProperty("event", "status_changed");
                payload.addProperty("actor_label", SupabaseCommunityService.getSignedInAlphaName());
                SupabaseCommunityService.createStaffOperationsRecord("presence", SupabaseCommunityService.getSignedInAlphaName() + " - " + command, presenceStatus, payload);
                SupabaseCommunityService.recordStaffModuleActivity("staff_presence", "action_performed", "presence_status_" + command, SupabaseCommunityService.getSignedInAlphaName(), payload);
                status = "Presence status saved.";
                output.add(command + (note.isEmpty() ? "" : ": " + note));
            } else if ("activity".equals(command)) {
                JsonArray events = SupabaseCommunityService.getStaffModuleActivity();
                String filter = args.length > 1 ? args[1].toLowerCase(java.util.Locale.ROOT) : "";
                int count = 0;
                for (int i = 0; i < events.size() && count < 12; i++) {
                    if (!events.get(i).isJsonObject()) continue;
                    JsonObject event = events.get(i).getAsJsonObject();
                    String actor = value(event, "actor_label", value(event, "actor_id", "Unknown staff"));
                    if (!filter.isEmpty() && !actor.toLowerCase(java.util.Locale.ROOT).contains(filter)) continue;
                    output.add(actor + " / " + value(event, "module_key", "module") + " / " + value(event, "action_key", "activity"));
                    count++;
                }
                status = count == 0 ? "No matching activity." : "Latest activity listed.";
            } else if ("loginlog".equals(command)) {
                JsonArray events = SupabaseCommunityService.getStaffModuleActivity();
                int count = 0;
                for (int i = 0; i < events.size() && count < 12; i++) {
                    if (!events.get(i).isJsonObject()) continue;
                    JsonObject event = events.get(i).getAsJsonObject();
                    String action = value(event, "action_key", "");
                    if (!"session_login".equals(action) && !"session_logout".equals(action)) continue;
                    output.add(value(event, "actor_label", value(event, "actor_id", "Unknown staff")) + " / " + action + " / " + value(event, "occurred_at", ""));
                    count++;
                }
                status = count == 0 ? "No login/logout events found." : "Login history listed.";
            } else if ("notes".equals(command)) {
                if (args.length < 2) { status = "Usage: notes <player>"; return; }
                JsonArray notes = SupabaseCommunityService.getStaffPlayerNotes(args[1].trim());
                int count = 0;
                for (int i = 0; i < notes.size() && count < 12; i++) {
                    if (!notes.get(i).isJsonObject()) continue;
                    JsonObject note = notes.get(i).getAsJsonObject();
                    output.add(value(note, "player_name", args[1]) + " / " + value(note, "created_by_label", "Staff") + " / " + value(note, "note", ""));
                    count++;
                }
                status = count == 0 ? "No notes found." : "Notes listed.";
            } else if ("addnote".equals(command)) {
                if (args.length < 3 || args[1].trim().isEmpty() || args[2].trim().isEmpty()) { status = "Usage: addnote <player> <note>"; return; }
                JsonObject note = SupabaseCommunityService.createStaffPlayerNote(args[1].trim(), args[2].trim());
                output.add(value(note, "player_name", args[1]) + " / " + value(note, "note", args[2]));
                status = "Staff note saved.";
            } else { status = "Unknown command: " + args[0] + ". Enter help."; }
        } catch (Exception e) {
            status = "Command failed: " + (e.getMessage() == null ? "request error" : e.getMessage().replace('\n', ' '));
        }
        if (output.size() > 100) output.subList(0, output.size() - 100).clear();
        input.setText("");
        input.setFocused(true);
    }

    private String value(JsonObject object, String key, String fallback) {
        try { return object != null && object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback; }
        catch (Exception ignored) { return fallback; }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        if (input != null && input.isFocused()) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) { execute(input.getText()); return; }
            input.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (input != null) {
            input.mouseClicked(mouseX, mouseY, mouseButton);
            boolean inside = mouseX >= input.xPosition && mouseX < input.xPosition + input.width
                    && mouseY >= input.yPosition && mouseY < input.yPosition + input.height;
            input.setFocused(inside && mouseButton == 0);
        }
    }
}
