// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.alphaclient.AlphaClient;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Founder-only command permission editor. Opened only through /alphaperms open. */
public final class AlphaPermsGui extends GuiScreen {
    private final GuiScreen parent;
    private GuiTextField targetField;
    private final List<PermissionRow> rows = new ArrayList<PermissionRow>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private boolean rankTarget = false;
    private String status = "Enter an Alpha account name, then click Load.";
    private boolean busy = false;
    private int listTop;
    private int listBottom;

    public AlphaPermsGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        targetField = new GuiTextField(0, fontRendererObj, center - 150, 42, 205, 20);
        targetField.setMaxStringLength(64);
        targetField.setFocused(true);
        buttonList.add(new GuiButton(1, center + 60, 42, 90, 20, rankTarget ? "Target: Rank" : "Target: Account"));
        buttonList.add(new GuiButton(2, center + 155, 42, 55, 20, "Load"));
        buttonList.add(new GuiButton(3, center - 210, height - 34, 95, 20, "Grant"));
        buttonList.add(new GuiButton(4, center - 110, height - 34, 95, 20, "Revoke"));
        buttonList.add(new GuiButton(5, center - 10, height - 34, 95, 20, "Clear Override"));
        buttonList.add(new GuiButton(6, center + 90, height - 34, 120, 20, "Refresh"));
        buttonList.add(new GuiButton(7, 8, height - 34, 65, 20, "Back"));
        loadCatalog();
    }

    private void loadCatalog() {
        if (busy) return;
        busy = true;
        status = "Loading permission catalog...";
        try {
            JsonObject request = new JsonObject();
            request.addProperty("action", "list");
            request.addProperty("target", "catalog");
            installRows(SupabaseCommunityService.manageCommandPermissions(request));
            status = "Catalog loaded. Select a permission, choose a target, then Load.";
        } catch (Exception exception) {
            status = message(exception);
        } finally {
            busy = false;
        }
    }

    private void loadTarget() {
        String target = targetField.getText().trim();
        if (target.isEmpty()) {
            status = rankTarget ? "Enter a rank name." : "Enter an Alpha account name.";
            return;
        }
        if (busy) return;
        busy = true;
        status = "Loading permissions for " + target + "...";
        try {
            JsonObject request = new JsonObject();
            request.addProperty("action", "list");
            if (rankTarget) {
                request.addProperty("target", "rank");
                request.addProperty("rank", target);
            } else {
                request.addProperty("username", target);
            }
            installRows(SupabaseCommunityService.manageCommandPermissions(request));
            status = "Loaded permissions for " + target + ". Select a row and choose Grant, Revoke, or Clear Override.";
        } catch (Exception exception) {
            status = message(exception);
        } finally {
            busy = false;
        }
    }

    private void applyAction(String action) {
        String target = targetField.getText().trim();
        if (target.isEmpty()) {
            status = rankTarget ? "Enter a rank name first." : "Enter an Alpha account name first.";
            return;
        }
        if (selectedIndex < 0 || selectedIndex >= rows.size()) {
            status = "Select a permission from the list first.";
            return;
        }
        PermissionRow selected = rows.get(selectedIndex);
        if (selected.key == null || selected.key.trim().isEmpty()) {
            status = "The selected row has no permission key.";
            return;
        }
        if (busy) return;
        busy = true;
        status = action.substring(0, 1).toUpperCase(Locale.ROOT) + action.substring(1) + " " + selected.key + "...";
        try {
            JsonObject request = new JsonObject();
            request.addProperty("permission", selected.key);
            if (rankTarget) {
                request.addProperty("rank", target);
                request.addProperty("action", "grant".equals(action) ? "grant_rank"
                        : ("revoke".equals(action) ? "revoke_rank" : "clear_rank"));
            } else {
                request.addProperty("username", target);
                request.addProperty("action", "grant".equals(action) ? "grant_user"
                        : ("revoke".equals(action) ? "revoke_user" : "clear_user"));
            }
            SupabaseCommunityService.manageCommandPermissions(request);
            status = "Updated " + selected.key + " for " + target + ".";
            busy = false;
            loadTarget();
        } catch (Exception exception) {
            status = message(exception);
        } finally {
            busy = false;
        }
    }

    private void installRows(JsonObject response) {
        rows.clear();
        selectedIndex = -1;
        JsonElement value = response == null ? null : response.get("permissions");
        if (value == null || !value.isJsonArray()) {
            throw new IllegalStateException("The permission service returned no permission list.");
        }
        JsonArray array = value.getAsJsonArray();
        for (JsonElement element : array) {
            if (element == null || !element.isJsonObject()) continue;
            JsonObject object = element.getAsJsonObject();
            rows.add(new PermissionRow(string(object, "permission_key"), string(object, "display_name"),
                    string(object, "category"), bool(object, "configured"), bool(object, "allowed"),
                    string(object, "source"), string(object, "description")));
        }
        scrollOffset = 0;
    }

    private String message(Exception exception) {
        String value = exception == null ? null : exception.getMessage();
        return value == null || value.trim().isEmpty() ? "Permission request failed." : value;
    }

    private static String string(JsonObject object, String key) {
        try {
            JsonElement value = object.get(key);
            return value == null || value.isJsonNull() ? null : value.getAsString();
        } catch (Exception ignored) { return null; }
    }

    private static boolean bool(JsonObject object, String key) {
        try {
            JsonElement value = object.get(key);
            return value != null && !value.isJsonNull() && value.getAsBoolean();
        } catch (Exception ignored) { return false; }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null || !button.enabled) return;
        if (button.id == 1) {
            rankTarget = !rankTarget;
            button.displayString = rankTarget ? "Target: Rank" : "Target: Account";
            status = rankTarget ? "Enter a rank key/name, then Load." : "Enter an Alpha account name, then Load.";
        } else if (button.id == 2 || button.id == 6) {
            loadTarget();
        } else if (button.id == 3) {
            applyAction("grant");
        } else if (button.id == 4) {
            applyAction("revoke");
        } else if (button.id == 5) {
            applyAction("clear");
        } else if (button.id == 7) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (targetField != null && targetField.isFocused()) {
            if (keyCode == 28 || keyCode == 156) {
                loadTarget();
                return;
            }
            targetField.textboxKeyTyped(typedChar, keyCode);
        }
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        targetField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && mouseY >= listTop && mouseY < listBottom) {
            int row = (mouseY - listTop) / 18 + scrollOffset;
            if (row >= 0 && row < rows.size()) selectedIndex = row;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && !rows.isEmpty()) {
            scrollOffset += wheel > 0 ? -3 : 3;
            int visible = Math.max(1, (listBottom - listTop) / 18);
            scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, rows.size() - visible)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "AlphaClient Permission Manager", width / 2, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Founder only  |  Account and rank permissions", width / 2, 26, 0xC8C8C8);
        targetField.drawTextBox();
        drawString(fontRendererObj, "Target name / rank", width / 2 - 150, 32, 0xA0A0A0);

        int left = Math.max(8, width / 2 - 235);
        int right = Math.min(width - 8, width / 2 + 235);
        listTop = 76;
        listBottom = height - 62;
        drawRect(left, listTop - 2, right, listBottom + 1, 0xAA111111);
        drawString(fontRendererObj, "CATEGORY / PERMISSION", left + 6, listTop - 14, 0xFFD45A);
        drawString(fontRendererObj, "STATUS", right - 95, listTop - 14, 0xFFD45A);

        int visible = Math.max(0, (listBottom - listTop) / 18);
        for (int i = 0; i < visible && i + scrollOffset < rows.size(); i++) {
            int index = i + scrollOffset;
            PermissionRow row = rows.get(index);
            int y = listTop + i * 18;
            if (index == selectedIndex) drawRect(left + 1, y, right - 1, y + 17, 0xAA6A5018);
            if ((i & 1) == 1 && index != selectedIndex) drawRect(left + 1, y, right - 1, y + 17, 0x44222222);
            String category = row.category == null ? "Other" : row.category;
            String name = row.display == null ? row.key : row.display;
            String leftText = "[" + category + "] " + name + "  (" + row.key + ")";
            drawString(fontRendererObj, trim(leftText, right - left - 120), left + 6, y + 4, 0xE6E6E6);
            String state = !row.configured ? "DEFAULT" : (row.allowed ? "ALLOW" : "DENY");
            int stateColor = !row.configured ? 0xB0B0B0 : (row.allowed ? 0x55DD77 : 0xFF7777);
            drawString(fontRendererObj, state, right - 92, y + 4, stateColor);
        }
        if (rows.isEmpty()) drawCenteredString(fontRendererObj, "No permissions loaded.", width / 2, listTop + 12, 0xAAAAAA);
        drawString(fontRendererObj, status, 8, height - 51, 0xFFFFFF);
        if (selectedIndex >= 0 && selectedIndex < rows.size()) {
            PermissionRow selected = rows.get(selectedIndex);
            if (selected.description != null && !selected.description.isEmpty()) {
                drawString(fontRendererObj, trim(selected.description, width - 16), 8, height - 40, 0xBEBEBE);
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String trim(String value, int maxPixels) {
        if (value == null) return "";
        String result = value;
        while (!result.isEmpty() && fontRendererObj.getStringWidth(result) > maxPixels) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static final class PermissionRow {
        private final String key;
        private final String display;
        private final String category;
        private final boolean configured;
        private final boolean allowed;
        private final String source;
        private final String description;

        private PermissionRow(String key, String display, String category, boolean configured,
                              boolean allowed, String source, String description) {
            this.key = key;
            this.display = display;
            this.category = category;
            this.configured = configured;
            this.allowed = allowed;
            this.source = source;
            this.description = description;
        }
    }
}
