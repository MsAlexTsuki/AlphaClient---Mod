package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** Personal moderation notes. Supabase RLS determines which notes this account can see. */
public final class PrivateStaffNotesGui extends AlphaGuiScreen {
    private static final int BACK = 1, REFRESH = 2, CREATE = 3;
    private static final int ACCENT = 0xFFFFD866;
    private final net.minecraft.client.gui.GuiScreen parent;
    private GuiTextField playerField;
    private GuiTextField noteField;
    private JsonArray notes = new JsonArray();
    private String status = "Loading private notes...";
    private int left, top, panelWidth, panelHeight;

    public PrivateStaffNotesGui(net.minecraft.client.gui.GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        if (!SupabaseCommunityService.hasStaffOperationsAccess()) { mc.displayGuiScreen(parent); return; }
        panelWidth = Math.min(650, Math.max(360, width - 28));
        panelHeight = Math.min(390, Math.max(280, height - 24));
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        playerField = new GuiTextField(20, fontRendererObj, left + 16, top + 82, panelWidth - 32, 20);
        playerField.setMaxStringLength(16);
        noteField = new GuiTextField(21, fontRendererObj, left + 16, top + 126, panelWidth - 32, 20);
        noteField.setMaxStringLength(2000);
        buttonList.add(new AlphaButton(REFRESH, left + 16, top + panelHeight - 58, 80, 20, "Refresh"));
        buttonList.add(new AlphaButton(CREATE, left + 102, top + panelHeight - 58, 110, 20, "Save private note", true));
        buttonList.add(new AlphaButton(BACK, left + panelWidth - 96, top + panelHeight - 30, 80, 20, "Back"));
        refreshNotes();
    }

    private void refreshNotes() {
        if (!SupabaseCommunityService.hasStaffOperationsAccess()) {
            notes = new JsonArray(); status = "Verified staff access is required."; return;
        }
        try {
            notes = SupabaseCommunityService.getPrivateStaffNotes(null);
            status = "Loaded " + notes.size() + " note(s) visible to this account.";
        } catch (Exception e) {
            status = "Could not load notes: " + safe(e.getMessage());
        }
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (!SupabaseCommunityService.hasStaffOperationsAccess()) { mc.displayGuiScreen(parent); return; }
        if (button.id == REFRESH) { refreshNotes(); return; }
        if (button.id == CREATE) {
            String player = playerField == null ? "" : playerField.getText().trim();
            String note = noteField == null ? "" : noteField.getText().trim();
            try {
                SupabaseCommunityService.createPrivateStaffNote(player, note);
                if (noteField != null) noteField.setText("");
                refreshNotes();
                status = "Private note saved.";
            } catch (Exception e) {
                status = "Save denied or failed: " + safe(e.getMessage());
            }
        }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (playerField != null) playerField.textboxKeyTyped(typedChar, keyCode);
        if (noteField != null) noteField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (playerField != null) playerField.mouseClicked(mouseX, mouseY, mouseButton);
        if (noteField != null) noteField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x30000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xE00B0D12);
        drawRect(left, top, left + panelWidth, top + 1, ACCENT);
        drawString(fontRendererObj, "Alpha  /  Staff operations", left + 16, top + 13, 0xFFF0F1F4);
        drawString(fontRendererObj, "Private staff notes", left + 16, top + 31, ACCENT);
        drawString(fontRendererObj, "Managers see their own notes. Owner/Founder can oversee all notes.", left + 16, top + 51, 0xFFB8BDC8);
        drawString(fontRendererObj, "Minecraft player", left + 16, top + 72, 0xFFE6E8ED);
        if (playerField != null) playerField.drawTextBox();
        drawString(fontRendererObj, "Note (maximum 2000 characters)", left + 16, top + 116, 0xFFE6E8ED);
        if (noteField != null) noteField.drawTextBox();
        drawString(fontRendererObj, "Visible notes", left + 16, top + 157, ACCENT);
        int shown = 0;
        for (int i = 0; i < notes.size() && shown < 6; i++) {
            if (!notes.get(i).isJsonObject()) continue;
            JsonObject item = notes.get(i).getAsJsonObject();
            String author = value(item, "created_by_label", "Staff");
            String line = value(item, "subject_name", "Unknown") + " / " + author + " / " + value(item, "note", "");
            if (line.length() > 94) line = line.substring(0, 91) + "...";
            drawString(fontRendererObj, line, left + 16, top + 176 + shown * 17, 0xFFE6E8ED);
            shown++;
        }
        if (shown == 0) drawString(fontRendererObj, "No private notes are visible to this account.", left + 16, top + 176, 0xFFB8BDC8);
        drawString(fontRendererObj, "Status: " + status, left + 16, top + panelHeight - 20, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String value(JsonObject object, String key, String fallback) {
        try { return object != null && object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback; }
        catch (Exception ignored) { return fallback; }
    }
    private String safe(String value) {
        if (value == null || value.trim().isEmpty()) return "request failed";
        value = value.replace('\n', ' ').replace('\r', ' ');
        return value.length() > 100 ? value.substring(0, 100) : value;
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}